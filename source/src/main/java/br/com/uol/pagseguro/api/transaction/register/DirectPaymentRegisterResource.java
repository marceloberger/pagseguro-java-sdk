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

package br.com.uol.pagseguro.api.transaction.register;

import br.com.uol.pagseguro.api.Endpoints;
import br.com.uol.pagseguro.api.PagSeguro;
import br.com.uol.pagseguro.api.common.domain.Bank;
import br.com.uol.pagseguro.api.common.domain.CreditCard;
import br.com.uol.pagseguro.api.common.domain.TransactionMethod;
import br.com.uol.pagseguro.api.common.domain.converter.BankV2MapConverter;
import br.com.uol.pagseguro.api.common.domain.converter.CreditCardV2MapConverter;
import br.com.uol.pagseguro.api.exception.PagSeguroLibException;
import br.com.uol.pagseguro.api.http.HttpClient;
import br.com.uol.pagseguro.api.http.HttpMethod;
import br.com.uol.pagseguro.api.http.HttpResponse;
import br.com.uol.pagseguro.api.transaction.search.TransactionDetail;
import br.com.uol.pagseguro.api.transaction.search.TransactionDetailXML;
import br.com.uol.pagseguro.api.utils.Builder;
import br.com.uol.pagseguro.api.utils.CharSet;
import br.com.uol.pagseguro.api.utils.Loggable;
import br.com.uol.pagseguro.api.utils.RequestMap;

import java.io.IOException;


/**
 * Factory to register direct payments.
 * You can create direct payments with bank slip,
 * online debit, credit card national and international credit card
 *
 * @author PagSeguro Internet Ltda.
 */
public class DirectPaymentRegisterResource implements Loggable {

    private static final CreditCardV2MapConverter CREDIT_CARD_MC = new CreditCardV2MapConverter();
    private static final DirectPaymentRegistrationV2MapConverter DIRECT_PAYMENT_REGISTRATION_MC = new DirectPaymentRegistrationV2MapConverter();
    private static final BankV2MapConverter BANK_MC = new BankV2MapConverter();

    private final PagSeguro pagSeguro;
    private final HttpClient httpClient;
    private final DirectPaymentRegistration directPaymentRegistration;

    /**
     * Constructor
     *
     * @param pagSeguro                 Pagseguro
     * @param httpClient                Http Client
     * @param directPaymentRegistration Interface with the attributes of Direct Payment Registration.
     */
    public DirectPaymentRegisterResource(PagSeguro pagSeguro, HttpClient httpClient, DirectPaymentRegistration directPaymentRegistration) {
        this.pagSeguro = pagSeguro;
        this.httpClient = httpClient;
        this.directPaymentRegistration = directPaymentRegistration;
    }

    /**
     * Execute direct payment with bank slip
     *
     * @return Response of direct payment registration
     * @see TransactionDetail
     */
    public TransactionDetail withBankSlip() {
        getLogger().debug("Iniciando pagamento direto com boleto");
        getLogger().debug("Convertendo valores");
        final RequestMap map = DIRECT_PAYMENT_REGISTRATION_MC.convert(directPaymentRegistration);
        map.putString("paymentMethod", TransactionMethod.BANK_SLIP.getName());
        getLogger().debug("Valores convertidos");
        final HttpResponse response;
        try {
            getLogger().debug(String.format("Parametros: %s", map));
            response = httpClient.execute(HttpMethod.POST, String.format(Endpoints.DIRECT_PAYMENT, pagSeguro.getHost()), null, map.toHttpRequestBody(CharSet.ENCODING_ISO));
            getLogger().debug(String.format("Resposta: %s", response.toString()));
        } catch (IOException e) {
            getLogger().error("Erro ao executar pagamento direto com boleto");
            throw new PagSeguroLibException(e);
        }
        getLogger().debug("Parseando XML de resposta");
        TransactionDetail transaction = response.parseXMLContent(pagSeguro, TransactionDetailXML.class);
        getLogger().debug("Parseamento finalizado");
        getLogger().debug("Pagamento direto com boleto finalizado");
        return transaction;
    }

    /**
     * Execute direct payment with credit card
     *
     * @param creditCard Interface with attributes of Credit Card
     * @return Response of direct payment registration
     * @see CreditCard
     * @see TransactionDetail
     */
    public TransactionDetail withCreditCard(CreditCard creditCard) {
        getLogger().info("Iniciando pagamento direto com cartao de credito");
        getLogger().info("Convertendo valores");
        final RequestMap map = DIRECT_PAYMENT_REGISTRATION_MC.convert(directPaymentRegistration);
        map.putString("paymentMethod", TransactionMethod.CREDIT_CARD.getName());
        map.putMap(CREDIT_CARD_MC.convert(creditCard));
        getLogger().info("Valores convertidos");
        final HttpResponse response;
        try {
            getLogger().debug(String.format("Parametros: %s", map));
            response = httpClient.execute(HttpMethod.POST,
                    String.format(Endpoints.DIRECT_PAYMENT, pagSeguro.getHost()), null,
                    map.toHttpRequestBody(CharSet.ENCODING_ISO));
            getLogger().debug(String.format("Resposta: %s", response.toString()));
        } catch (IOException e) {
            getLogger().error("Erro ao executar pagamento direto com cartao de credito");
            throw new PagSeguroLibException(e);
        }
        getLogger().info("Parseando XML de resposta");
        TransactionDetail transaction = response.parseXMLContent(pagSeguro, TransactionDetailXML.class);
        getLogger().info("Parseamento finalizado");
        getLogger().info("Pagamento direto com cartao de credito finalizado");
        return transaction;
    }

    /**
     * Execute direct payment with credit card
     *
     * @param creditCardBuilder Builder for attributes of Credit Card
     * @return Response of direct payment registration
     * @see CreditCard
     * @see TransactionDetail
     */
    public TransactionDetail withCreditCard(Builder<CreditCard> creditCardBuilder) {
        return withCreditCard(creditCardBuilder.build());
    }

    /**
     * Execute direct payment with international credit card
     *
     * @param internationalCreditCard Instance with attributes of International Credit Card
     * @return Response of direct payment registration
     * @see TransactionDetail
     * @see CreditCard
     */
    public TransactionDetail withInternationalCreditCard(CreditCard internationalCreditCard) {
        getLogger().info("Iniciando pagamento direto com cartao de credito internacional");
        getLogger().info("Convertendo valores");
        final RequestMap map = DIRECT_PAYMENT_REGISTRATION_MC.convert(directPaymentRegistration);
        map.putString("paymentMethod", TransactionMethod.CREDIT_CARD.getName());
        map.putMap(CREDIT_CARD_MC.convert(internationalCreditCard));
        getLogger().info("Valores convertidos");
        final HttpResponse response;
        try {
            getLogger().debug(String.format("Parametros: %s", map));
            response = httpClient.execute(HttpMethod.POST,
                    String.format(Endpoints.DIRECT_PAYMENT, pagSeguro.getHost()), null,
                    map.toHttpRequestBody(CharSet.ENCODING_ISO));
            getLogger().debug(String.format("Resposta: %s", response.toString()));
        } catch (IOException e) {
            getLogger().error("Erro ao executar pagamento direto com cartao de credito internacional");
            throw new PagSeguroLibException(e);
        }
        getLogger().info("Parseando XML de resposta");
        TransactionDetail transaction = response.parseXMLContent(pagSeguro, TransactionDetailXML.class);
        getLogger().info("Parseamento finalizado");
        getLogger().info("Pagamento direto com cartao de credito internacional finalizado");
        return transaction;
    }

    /**
     * Execute direct payment with international credit card
     *
     * @param internationalCreditCardBuilder Builder for attributes of International Credit Card
     * @return Response of direct payment registration
     * @see TransactionDetail
     * @see CreditCard
     */
    public TransactionDetail withInternationalCreditCard(
            Builder<CreditCard> internationalCreditCardBuilder) {
        return withInternationalCreditCard(internationalCreditCardBuilder.build());
    }

    /**
     * Execute direct payment with online debit
     *
     * @param bankBuilder Builder for attributes of bank
     * @return Response of direct payment registration
     * @see Bank
     * @see TransactionDetail
     */
    public TransactionDetail withOnlineDebit(Builder<Bank> bankBuilder) {
        return withOnlineDebit(bankBuilder.build());
    }

    /**
     * Execute direct payment with online debit
     *
     * @param bank Interface with attributes of bank slip
     * @return Response of direct payment registration
     * @see TransactionDetail
     * @see Bank
     */
    public TransactionDetail withOnlineDebit(Bank bank) {
        getLogger().info("Iniciando pagamento direto com debito online");
        getLogger().info("Convertendo valores");
        final RequestMap map = DIRECT_PAYMENT_REGISTRATION_MC.convert(directPaymentRegistration);
        map.putString("paymentMethod", TransactionMethod.ONLINE_DEBIT.getName());
        map.putMap(BANK_MC.convert(bank));
        getLogger().info("Valores convertidos");
        final HttpResponse response;
        try {
            getLogger().debug(String.format("Parametros: %s", map));
            response = httpClient.execute(HttpMethod.POST, String.format(Endpoints.DIRECT_PAYMENT, pagSeguro.getHost()), null, map.toHttpRequestBody(CharSet.ENCODING_ISO));
            getLogger().debug(String.format("Resposta: %s", response.toString()));
        } catch (IOException e) {
            getLogger().error("Erro ao executar pagamento direto com debito online");
            throw new PagSeguroLibException(e);
        }
        getLogger().info("Parseando XML de resposta");
        TransactionDetail transaction = response.parseXMLContent(pagSeguro, TransactionDetailXML.class);
        getLogger().info("Parseamento finalizado");
        getLogger().info("Pagamento direto com debito online finalizado");
        return transaction;
    }
}