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
package br.com.uol.pagseguro.api.transaction;

import br.com.uol.pagseguro.api.Endpoints;
import br.com.uol.pagseguro.api.PagSeguro;
import br.com.uol.pagseguro.api.exception.PagSeguroLibException;
import br.com.uol.pagseguro.api.http.HttpClient;
import br.com.uol.pagseguro.api.http.HttpMethod;
import br.com.uol.pagseguro.api.http.HttpResponse;
import br.com.uol.pagseguro.api.transaction.register.*;
import br.com.uol.pagseguro.api.transaction.search.TransactionSearchResource;
import br.com.uol.pagseguro.api.utils.Builder;
import br.com.uol.pagseguro.api.utils.CharSet;
import br.com.uol.pagseguro.api.utils.RequestMap;
import br.com.uol.pagseguro.api.utils.Loggable;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Factory to transactions
 *
 * @author PagSeguro Internet Ltda.
 */
public class TransactionsResource implements Loggable {

    private static final TransactionCancellationV2MapConverter TRANSACTION_CANCELATION_MC = new TransactionCancellationV2MapConverter();
    private static final TransactionIdentifyV2MapConverter TRANSACTION_IDENTIFY_MC = new TransactionIdentifyV2MapConverter();

    private final PagSeguro pagSeguro;
    private final HttpClient httpClient;

    public TransactionsResource(PagSeguro pagSeguro, HttpClient httpClient) {
        this.pagSeguro = pagSeguro;
        this.httpClient = httpClient;
    }

    /**
     * Factory to Search transactions
     *
     * @return Search Transactions
     * @see TransactionSearchResource
     */
    public TransactionSearchResource search() {
        return new TransactionSearchResource(pagSeguro, httpClient);
    }

    /**
     * Execute the transaction cancellation
     *
     * @param transaction Transaction identify
     * @see TransactionIdentify
     */
    public void cancel(TransactionIdentify transaction) {
        getLogger().info("Iniciando cancelamento de transacao");
        getLogger().info("Convertendo valores");
        final RequestMap map = TRANSACTION_CANCELATION_MC.convert(transaction);
        getLogger().info("Valores convertidos");
        final HttpResponse response;
        try {
            getLogger().debug(String.format("Parametros: %s", map));
            response = httpClient.execute(HttpMethod.POST,
                    String.format(Endpoints.TRANSACTION_CANCEL, pagSeguro.getHost()), null,
                    map.toHttpRequestBody(CharSet.ENCODING_ISO));
            getLogger().debug(String.format("Resposta: %s", response.toString()));
        } catch (IOException e) {
            getLogger().error("Erro ao executar cancelamento de transacao");
            throw new PagSeguroLibException(e);
        }
        getLogger().info("Parseando XML de resposta");
        response.parseXMLContent(pagSeguro, CancelResponseXMLTransaction.class);
        getLogger().info("Parseamento finalizado");
        getLogger().info("Cancelamento de transacao finalizado");
    }

    /**
     * Execute the transaction cancellation
     *
     * @param transactionBuilder Builder for Transaction identify
     * @see TransactionIdentify
     */
    public void cancel(Builder<TransactionIdentify> transactionBuilder) {
        cancel(transactionBuilder.build());
    }

    /**
     * Execute the transaction cancellation by code
     *
     * @param code Transaction code
     * @see TransactionIdentify
     */
    public void cancelByCode(String code) {
        cancel(new TransactionIdentifyBuilder().withCode(code).build());
    }

    /**
     * Execute the transaction refunding
     *
     * @param transactionIdentify Transaction Identify
     * @param amount              Amount of refund
     * @see TransactionIdentify
     */
    public void refund(TransactionIdentify transactionIdentify, BigDecimal amount) {
        getLogger().info("Iniciando estorno de transacao");
        getLogger().info("Convertendo valores");
        final RequestMap map = TRANSACTION_IDENTIFY_MC.convert(transactionIdentify);
        map.putCurrency("refundValue", amount);
        getLogger().info("Valores convertidos");
        final HttpResponse response;
        try {
            getLogger().debug(String.format("Parametros: %s", map));
            response = httpClient.execute(HttpMethod.POST,
                    String.format(Endpoints.TRANSACTION_REFUND, pagSeguro.getHost()), null,
                    map.toHttpRequestBody(CharSet.ENCODING_ISO));
            getLogger().debug(String.format("Resposta: %s", response.toString()));
        } catch (IOException e) {
            getLogger().error("Erro ao executar estorno de transacao");
            throw new PagSeguroLibException(e);
        }
        getLogger().info("Parseando XML de resposta");
        response.parseXMLContent(pagSeguro, RefundResponseXMLTransaction.class);
        getLogger().info("Parseamento finalizado");
        getLogger().info("Estorno de transacao finalizado");
    }

    /**
     * Execute the transaction refunding without amount
     *
     * @param transactionIdentify Transaction Identify
     * @see TransactionIdentify
     */
    public void refund(TransactionIdentify transactionIdentify) {
        refund(transactionIdentify, null);
    }

    /**
     * Execute the transaction refunding
     *
     * @param transactionIdentifyBuilder Builder for Transaction Identify
     * @param amount                     Amount of refund
     * @see TransactionIdentify
     */
    public void refund(Builder<TransactionIdentify> transactionIdentifyBuilder, BigDecimal amount) {
        refund(transactionIdentifyBuilder.build(), null);
    }

    /**
     * Execute the transaction refunding without amount
     *
     * @param transactionIdentifyBuilder Builder for Transaction Identify
     * @see TransactionIdentify
     */
    public void refund(Builder<TransactionIdentify> transactionIdentifyBuilder) {
        refund(transactionIdentifyBuilder.build(), null);
    }

    /**
     * Execute the transaction refunding by code
     *
     * @param code Transaction code
     * @see TransactionIdentify
     */
    public void refundByCode(String code) {
        refund(new TransactionIdentifyBuilder().withCode(code).build(), null);
    }

    /**
     * Execute the transaction refunding by code and amount
     *
     * @param code   Transaction code
     * @param amount Amount of refund
     * @see TransactionIdentify
     */
    public void refundByCode(String code, BigDecimal amount) {
        refund(new TransactionIdentifyBuilder().withCode(code).build(), amount);
    }

    /**
     * Factory to execute split payment registration
     *
     * @param splitPaymentRegistrationBuilder Builder for split payment registration
     * @return Factory to execute split payment  registration
     * @see SplitPaymentRegistrationBuilder
     * @see SplitPaymentRegisterResource
     */
    public SplitPaymentRegisterResource register(
            SplitPaymentRegistrationBuilder splitPaymentRegistrationBuilder) {
        return register(splitPaymentRegistrationBuilder.build());
    }

    /**
     * Factory to execute split payment registration
     *
     * @param splitPaymentRegistration Split Payment registration
     * @return Factory to execute split payment registration
     * @see SplitPaymentRegistration
     * @see SplitPaymentRegisterResource
     */
    public SplitPaymentRegisterResource register(SplitPaymentRegistration splitPaymentRegistration) {
        return new SplitPaymentRegisterResource(pagSeguro, httpClient, splitPaymentRegistration);
    }

    /**
     * Factory to execute direct payment registration
     *
     * @param directPaymentRegistrationBuilder Builder for direct payment registration
     * @return Factory to execute direct payment registration
     * @see DirectPaymentRegistrationBuilder
     * @see DirectPaymentRegisterResource
     */
    public DirectPaymentRegisterResource register(
            DirectPaymentRegistrationBuilder directPaymentRegistrationBuilder) {
        return register(directPaymentRegistrationBuilder.build());
    }


    /**
     * Factory to execute direct payment registration
     *
     * @param directPaymentRegistration Direct payment registration
     * @return Factory to execute direct payment registration
     * @see DirectPaymentRegistration
     * @see DirectPaymentRegisterResource
     */
    public DirectPaymentRegisterResource register(
            DirectPaymentRegistration directPaymentRegistration) {
        return new DirectPaymentRegisterResource(pagSeguro, httpClient, directPaymentRegistration);
    }
}
