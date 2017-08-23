/*
 * 2007-2016 [PagSeguro Internet Ltda.]
 *
 * NOTICE OF LICENSE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright: 2007-2016 PagSeguro Internet Ltda.
 * Licence: http://www.apache.org/licenses/LICENSE-2.0
 */
package br.com.uol.pagseguro.api.http;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * JSE Http client.
 * Responsible for communicating with the api.
 *
 * @author PagSeguro Internet Ltda.
 */
public class JSEHttpClient implements HttpClient {

    private static String DEFAULT_RESPONSE_CHARSET = "ISO-8859-1";
    private final static String LIB_VERSION = "3.1.1";

    /**
     * Execute the communication with api.
     *
     * @param method    Http method
     * @param targetURL target url
     * @param headers   Headers
     * @param body      Body
     * @return Http Response
     */
    @Override
    public HttpResponse execute(HttpMethod method, String targetURL, Map<String, String> headers,
                                HttpRequestBody body) throws IOException {

        getLogger().info(String.format("Executando [%s] em [%s]", method.toString(), targetURL));

        HttpURLConnection connection = null;
        try {
            final URL url = new URL(targetURL);
            getLogger().info("Abrindo conexao");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method.toString());
            connection.setUseCaches(false);

            writeHeaders(connection, headers);
            getLogger().info("Escrevendo body");
            writeBody(connection, body);

            // le response response
            final String responseCharset = getResponseCharset(connection);
            getLogger().info("Lendo resposta");
            final InputStream responseStream = getResponseStream(connection);
            final String responseString = getResponseString(responseStream, responseCharset);
            return new HttpResponse(connection.getResponseCode(), responseString);
        } finally {
            if (connection != null) {
                getLogger().info("Fechando conexao");
                connection.disconnect();
            }
        }
    }

    /**
     * Write headers on request
     *
     * @param connection Connection
     * @param headers    Headers
     */
    private static void writeHeaders(HttpURLConnection connection, Map<String, String> headers) {
        if (headers == null) {
            headers = new HashMap<String, String>();
        }

        headers.put("lib-description", "java:" + LIB_VERSION);
        headers.put("language-engine-description", "java:" + System.getProperty("java.version"));

        for (Entry<String, String> e : headers.entrySet()) {
            connection.setRequestProperty(e.getKey(), e.getValue());
        }

    }

    /**
     * Write body on request
     *
     * @param connection Connection
     * @param body       Body
     */
    private static void writeBody(HttpURLConnection connection, HttpRequestBody body) throws
            IOException {
        if (body == null) {
            return;
        }
        final String charset = body.getCharset();
        final String content = body.getContent();
        //connection.setRequestProperty("Content-Type", body.getContentTypeWithCharset());
        //connection.setRequestProperty("Content-Length", Integer.toString(content.getBytes(charset).length));
        connection.setDoOutput(true);

        DataOutputStream wr = null;
        BufferedWriter buffWr = null;
        try {
            wr = new DataOutputStream(connection.getOutputStream());
            buffWr = new BufferedWriter(new OutputStreamWriter(wr, charset));
            buffWr.write(content);
        } finally {
            if (buffWr != null) {
                buffWr.close();
            }
            if (wr != null) {
                wr.close();
            }
        }
    }

    /**
     * Get response as string
     *
     * @param responseStream  Response Stream
     * @param responseCharset Response charset
     * @return Response string
     */
    private static String getResponseString(InputStream responseStream, String responseCharset) throws
            IOException {
        if (responseStream == null || responseCharset == null) {
            throw new IllegalArgumentException();
        }
        BufferedReader rd = null;
        try {
            rd = new BufferedReader(new InputStreamReader(responseStream, responseCharset));
            final StringBuilder responseString = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                responseString.append(line);
            }
            return responseString.toString();
        } finally {
            if (rd != null) {
                rd.close();
            }
        }
    }

    /**
     * Get response stream
     *
     * @param connection Connection
     * @return Input Stream
     */
    private static InputStream getResponseStream(HttpURLConnection connection) throws IOException {
        if (connection == null) {
            throw new IllegalArgumentException();
        }
        InputStream errorStream = connection.getErrorStream();
        if (errorStream != null) {
            return errorStream;
        }
        return connection.getInputStream();
    }

    /**
     * Get response charset
     *
     * @param connection Coonection
     * @return Response charset
     */
    private static String getResponseCharset(HttpURLConnection connection) {
        if (connection == null) {
            throw new IllegalArgumentException();
        }
        final String contentType = connection.getContentType();
        if (contentType == null) {
            return DEFAULT_RESPONSE_CHARSET;
        }
        final String[] values = contentType.split(";");

        if (values == null) {
            return DEFAULT_RESPONSE_CHARSET;
        }
        for (String value : values) {
            value = value.trim();
            if (value.toLowerCase().startsWith("charset=")) {
                return value.substring("charset=".length());
            }
        }
        return DEFAULT_RESPONSE_CHARSET;
    }
}
