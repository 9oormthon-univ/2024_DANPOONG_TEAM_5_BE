package com.jangburich.domain.barobill.application;

import com.baroservice.api.BarobillApiService;
import com.baroservice.ws.ArrayOfTaxInvoiceTradeLineItem;
import com.baroservice.ws.InvoiceParty;
import com.baroservice.ws.TaxInvoice;
import com.baroservice.ws.TaxInvoiceTradeLineItem;
import com.jangburich.domain.barobill.dto.request.GetCertificateRegistURLRequest;
import com.jangburich.domain.barobill.dto.request.RegistAndReverseIssueTaxInvoiceRequest;
import com.jangburich.domain.barobill.dto.request.RegistCorpRequest;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BarobillService {

    private final BarobillApiService barobillApiService;
    private static final String CERT_KEY = "164D08BA-4305-4256-8E8C-0BD955D08862";

    @Transactional
    public void registCorp(RegistCorpRequest registCorpRequest) {
        int result = barobillApiService.taxInvoice.registCorp(CERT_KEY, registCorpRequest.corpNum(),
                registCorpRequest.corpName(), registCorpRequest.ceoName(), registCorpRequest.bizType(), registCorpRequest.bizClass(),
                registCorpRequest.postNum(), registCorpRequest.addr1(),
                registCorpRequest.addr2(), registCorpRequest.memberName(), registCorpRequest.juminNum(), registCorpRequest.id(), registCorpRequest.pwd(), registCorpRequest.grade(),
                registCorpRequest.tel(), registCorpRequest.hp(), registCorpRequest.email());

        if (result < 0) { // 호출 실패
            System.out.println(result);
        } else { // 호출 성공
            System.out.println(result);
        }
    }

    public void getCertificateRegistURL(GetCertificateRegistURLRequest getCertificateRegistURLRequest) {
        String result = barobillApiService.taxInvoice.getCertificateRegistURL(CERT_KEY,
                getCertificateRegistURLRequest.corpNum(), getCertificateRegistURLRequest.id(),
                getCertificateRegistURLRequest.pwd());

        if (Pattern.compile("^-[0-9]{5}").matcher(result).matches()) { // 호출 실패
            System.out.println(result);
        } else { // 호출 성공
            System.out.println(result);
        }
    }

    @Transactional
    public void registAndReverseIssueTaxInvoice(
            RegistAndReverseIssueTaxInvoiceRequest registAndReverseIssueTaxInvoiceRequest) {
        // 공급자, 공급받는자의 관리번호 채번
        String invoicerMgtNum = "000001-R";
        String invoiceeMgtNum = "000001-E";

        // 공급자, 공급받는자의 바로빌 아이디 불러오기
        String invoicerBarobillID = registAndReverseIssueTaxInvoiceRequest.invoicerBarobillID();
        String invoiceeBarobillID = registAndReverseIssueTaxInvoiceRequest.invoiceeBarobillID();

        // 세금계산서 내용을 담은 클래스(또는 구조체) 생성
        TaxInvoice taxInvoice = new TaxInvoice();

        /**/
        taxInvoice.setIssueDirection(2);
        taxInvoice.setTaxInvoiceType(1);

        taxInvoice.setModifyCode("");

        taxInvoice.setTaxType(1);
        taxInvoice.setTaxCalcType(1);
        taxInvoice.setPurposeType(1);

        taxInvoice.setWriteDate("20241129");

        taxInvoice.setAmountTotal("100");
        taxInvoice.setTaxTotal("100");
        taxInvoice.setTotalAmount("200");
        taxInvoice.setCash("");
        taxInvoice.setChkBill("");
        taxInvoice.setNote("");
        taxInvoice.setCredit("");

        taxInvoice.setRemark1("");
        taxInvoice.setRemark2("");
        taxInvoice.setRemark3("");

        taxInvoice.setKwon("");
        taxInvoice.setHo("");
        taxInvoice.setSerialNum("");



        /**/

        // 공급자 정보
        taxInvoice.setInvoicerParty(new InvoiceParty());
        taxInvoice.getInvoicerParty().setMgtNum(invoicerMgtNum);
        taxInvoice.getInvoicerParty().setCorpNum("");
        taxInvoice.getInvoicerParty().setTaxRegID("");
        taxInvoice.getInvoicerParty().setCorpName("partisbutchershop");
        taxInvoice.getInvoicerParty().setCEOName("");
        taxInvoice.getInvoicerParty().setAddr("");
        taxInvoice.getInvoicerParty().setBizType("한식");
        taxInvoice.getInvoicerParty().setBizClass("음식점업");
        taxInvoice.getInvoicerParty().setContactID(invoicerBarobillID);
        taxInvoice.getInvoicerParty().setContactName("");
        taxInvoice.getInvoicerParty().setTEL("");
        taxInvoice.getInvoicerParty().setHP("");
        taxInvoice.getInvoicerParty().setEmail("");

        //공급받는자 정보
        taxInvoice.setInvoiceeParty(new InvoiceParty());
        taxInvoice.getInvoiceeParty().setMgtNum(invoiceeMgtNum);
        taxInvoice.getInvoiceeParty().setCorpNum("");
        taxInvoice.getInvoiceeParty().setTaxRegID("");
        taxInvoice.getInvoiceeParty().setCorpName("Jangburich");
        taxInvoice.getInvoiceeParty().setCEOName("");
        taxInvoice.getInvoiceeParty().setAddr("");
        taxInvoice.getInvoiceeParty().setBizType("");
        taxInvoice.getInvoiceeParty().setBizClass("");
        taxInvoice.getInvoiceeParty().setContactID(invoiceeBarobillID);
        taxInvoice.getInvoiceeParty().setContactName("");
        taxInvoice.getInvoiceeParty().setTEL("");
        taxInvoice.getInvoiceeParty().setHP("");
        taxInvoice.getInvoiceeParty().setEmail("");

        // 수탁자 정보
        taxInvoice.setBrokerParty(new InvoiceParty());
        taxInvoice.getBrokerParty().setCorpNum("");
        taxInvoice.getBrokerParty().setTaxRegID("");
        taxInvoice.getBrokerParty().setCorpName("");
        taxInvoice.getBrokerParty().setCEOName("");
        taxInvoice.getBrokerParty().setAddr("");
        taxInvoice.getBrokerParty().setBizType("");
        taxInvoice.getBrokerParty().setBizClass("");
        taxInvoice.getBrokerParty().setContactID("");
        taxInvoice.getBrokerParty().setContactName("");
        taxInvoice.getBrokerParty().setTEL("");
        taxInvoice.getBrokerParty().setHP("");
        taxInvoice.getBrokerParty().setEmail("");

        // 품목
        taxInvoice.setTaxInvoiceTradeLineItems(new ArrayOfTaxInvoiceTradeLineItem());

        TaxInvoiceTradeLineItem taxInvoiceTradeLineItem = new TaxInvoiceTradeLineItem();
        taxInvoiceTradeLineItem.setPurchaseExpiry("");
        taxInvoiceTradeLineItem.setName("");
        taxInvoiceTradeLineItem.setInformation("");
        taxInvoiceTradeLineItem.setChargeableUnit("");
        taxInvoiceTradeLineItem.setUnitPrice("");
        taxInvoiceTradeLineItem.setAmount("");
        taxInvoiceTradeLineItem.setTax("");
        taxInvoiceTradeLineItem.setDescription("");

        taxInvoice.getTaxInvoiceTradeLineItems().getTaxInvoiceTradeLineItem().add(taxInvoiceTradeLineItem);

        int result = barobillApiService.taxInvoice.registAndReverseIssueTaxInvoice(CERT_KEY, "", taxInvoice,
                false,
                false, "");

        if (result < 0) { // API 호출 실패
            // 오류코드 내용에 따라 파라메터를 수정하여 다시 실행해주세요.
            String errMessage = barobillApiService.taxInvoice.getErrString(CERT_KEY, result);
            System.out.println(errMessage);
        } else { // 성공
            // 파트너 프로그램의 후 처리
            System.out.println("성공");
        }
    }
}
