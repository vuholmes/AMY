// Decompiled by DJ v3.5.5.77 Copyright 2003 Atanas Neshkov  Date: 9/9/2015 2:42:39 PM
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   ConsSalesStmtRptQueryBuilder_2.java

package qrcom.PROFIT.reports.CC;

import java.sql.*;
import qrcom.PROFIT.files.info.ProfitvvSQL;
import qrcom.util.qrMisc; 
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ConsSalesStmtRptQueryBuilder_2
{
    private String USER_COY = "";
    private String COY_SUB = ""; 
    private String SUPPL = "";
    private String SUPPL_CONT = "";
    private String FR_CONCESS_DATE = "";
    private String TO_CONCESS_DATE = "";

    public ConsSalesStmtRptQueryBuilder_2()
    {
        conn = null;
        resultSet = null;
        pstmt = null;
        profitvvSQL = null;
        SYSB2BPoAdjConcess = "";
        SYSCSSMGSReason = "";
        String SYSCSSGRNReason = "";
        String SYSCSSVarianceReason = "";
        String SYSCSSGRNADJReason = "";
    }

    public ConsSalesStmtRptQueryBuilder_2(Connection conn)
        throws Exception
    {
        this.conn = null;
        resultSet = null;
        pstmt = null;
        profitvvSQL = null;
        SYSB2BPoAdjConcess = "";
        SYSCSSMGSReason = "";
        String SYSCSSGRNReason = "";
        String SYSCSSVarianceReason = "";
        String SYSCSSGRNADJReason = "";
        this.conn = conn;
        jInit();
    }

    private void jInit()
        throws Exception
    {
        profitvvSQL = new ProfitvvSQL(conn);
    } 

    public String getConcessQuery(String strCoy, String strCoySub, String strSuppl, String strSupplCont, String strFrConcessDate, String strToConcessDate)
        throws Exception
    {
        
        
//        Date first_day = qrMisc.getFirstDateOfMonth(qrMisc.parseSqlDate(strConcessDate));
//        System.out.println("first_day = "+first_day);
//        Date last_day = qrMisc.getLastDateOfMonth(qrMisc.parseSqlDate(strConcessDate));
//        System.out.println("last_day = "+last_day);
        Date first_day = qrMisc.parseSqlDate(strFrConcessDate);
        Date last_day = qrMisc.parseSqlDate(strToConcessDate);
        SYSB2BPoAdjConcess = getProfitvvValue(strCoy, "SYSB2BPoAdjConcess");
        SYSCSSMGSReason = getProfitvvValue(strCoy, "SYSCSSMGSReason");
        SYSCSSGRNReason = getProfitvvValue(strCoy, "SYSCSSGRNReason");
        SYSCSSVarianceReason = getProfitvvValue(strCoy, "SYSCSSVarianceReason");
        SYSCSSGRNADJReason = getProfitvvValue(strCoy, "SYSCSSGRNADJReason");
//        SYSCSSMGSReason = "SYSCSSMGSReason";
//        SYSCSSGRNReason = "SYSCSSGRNReason";
//        SYSCSSVarianceReason = "SYSCSSVarianceReason";
//        SYSCSSGRNADJReason = "SYSCSSGRNADJReason";
        
        this.USER_COY = strCoy;
        this.COY_SUB = strCoySub;
        this.SUPPL = strSuppl;
        this.SUPPL_CONT = strSupplCont;
        this.FR_CONCESS_DATE = strFrConcessDate;
        this.TO_CONCESS_DATE = strToConcessDate;
        
        String strQueryAll = "";
//        String strQueryA = "";
//        String strQueryB = "";
//        String strQueryC = "";
//        String strFieldA = "";
//        String strFieldB = "";
//        String strFieldC = "";
//        String strEXTRA = "";
//        String strUNION = "UNION ";
//        String strORDER_BY = "ORDER BY SUPPL, SUPPL_CONTRACT, DOC_NO, ITEM_GRP, MARGIN, SHORT_SKU, ITEM_COST ASC ";
//        strFieldA = "AND P.PO_SUPPL = '" + strSuppl + "' ";
//        strFieldB = "AND P.PO_SUPPL_CONTRACT = '" + strSupplCont + "' ";
//        strFieldC = "AND P.DATE_ORDER = TO_DATE('" + strConcessDate + "', 'YYYY-MM-DD') ";
//        if(strSuppl != null && strSuppl.length() > 0)
//            strEXTRA = strEXTRA + strFieldA;
//        if(strSupplCont != null && strSupplCont.length() > 0)
//            strEXTRA = strEXTRA + strFieldB;
//        if(strConcessDate != null && strConcessDate.length() > 0)
//            strEXTRA = strEXTRA + strFieldC;
//        strQueryA = "SELECT P.ORD_NO AS DOC_NO, P.ORD_NO AS DOC_ID, P.DATE_ORDER AS DATE_TRN, P.PO_STORE AS STORE, P.PO_SUPPL AS SUPPL, P.PO_SUPPL_CONTRACT AS SUPPL_CONTRACT, P.PO_FRGN_CRNCY_CD AS CRNCY_CD, P.PO_GROUP AS ITEM_GRP, P.PO_RTL_VAT_RATE AS SVAT_RATE, P.PO_VAT_RATE AS VAT_RATE, D.SHORT_SKU, D.GPPER AS MARGIN, QTY_ORD AS QTY, D.ITEM_COST_ORD AS ITEM_COST, D.EXT_COST_ORD_WVAT AS EXT_COST_WVAT, D.EXT_SELL_ORD AS EXT_SELL, D.PO_SEQ AS SEQ, D.EXT_SELL_ORD_WOVAT AS EXT_SELL_WOVAT FROM POHDR P, PODET D WHERE P.ORD_STATUS = 'F' AND P.PO_TYPE = 'O' AND D.COY = P.COY AND D.ORD_NO = P.ORD_NO " + strEXTRA;
//        strQueryB = "SELECT P.ORD_NO AS DOC_NO, P.ORD_NO AS DOC_ID, P.DATE_ORDER AS DATE_TRN, P.PO_STORE AS STORE, P.PO_SUPPL AS SUPPL, P.PO_SUPPL_CONTRACT AS SUPPL_CONTRACT, P.PO_FRGN_CRNCY_CD AS CRNCY_CD, P.PO_GROUP AS ITEM_GRP, P.PO_RTL_VAT_RATE AS SVAT_RATE, P.PO_VAT_RATE AS VAT_RATE, D.SHORT_SKU, D.GPPER AS MARGIN, QTY_ORD AS QTY, D.ITEM_COST_ORD AS ITEM_COST, D.EXT_COST_ORD_WVAT AS EXT_COST_WVAT, D.EXT_SELL_ORD AS EXT_SELL, D.PO_SEQ AS SEQ, D.EXT_SELL_ORD_WOVAT AS EXT_SELL_WOVAT FROM POHDR P, PODET D, RECHDR R, POADJCHKLSTHDR A, VNCNTERM VN WHERE P.COY = R.COY AND P.ORD_NO = R.DOCUMENT_NO AND A.COY = P.COY AND A.COY_SUB = P.COY_SUB AND A.CHKLST_STORE = P.PO_STORE AND A.ADJ_ORD_NO = P.ORD_NO AND A.CHKLST_STATUS = 'R' AND VN.VN_CODE = P.PO_SUPPL AND VN.VN_CONTRACT = P.PO_SUPPL_CONTRACT AND VN.VN_SUBTYPE = 'C' AND D.COY = P.COY AND D.ORD_NO = P.ORD_NO " + strEXTRA;
//        strQueryC = "SELECT TO_CHAR(C.CRED_CLAIM_NO) AS DOC_NO, DEBIT_NOTE_NO AS DOC_ID, C.DATE_RTN AS DATE_TRN, C.STORE, C.CC_SUPPL AS SUPPL, C.CC_SUPPL_CONTRACT AS SUPPL_CONTRACT, C.CC_CRNCY_CD AS CRNCY_CD, C.CC_ITEM_GROUP AS ITEM_GRP, C.CC_SVAT_RATE AS SVAT_RATE, C.CC_VAT_RATE AS VAT_RATE, D.SHORT_SKU, D.CC_MARGIN AS MARGIN, SUM(D.QTY_RTN) AS QTY, SUM(D.EXT_COST_RTN) * -1 AS EXT_COST, SUM(D.EXT_COST_RTN_WVAT) * -1 AS EXT_COST_WVAT, SUM(D.EXT_SELL_RTN) * -1 AS EXT_SELL, D.CC_SEQ AS SEQ, SUM(D.EXT_SELL_RTN_WOVAT) * -1 AS EXT_SELL_WOVAT FROM CCTXHDR C, CCTXDET D WHERE C.CC_STATUS = 'C' AND CC_CREATION_METHOD = 'CNCES' AND D.STORE = C.STORE AND D.CRED_CLAIM_NO = C.CRED_CLAIM_NO " + strEXTRA + "GROUP BY C.CRED_CLAIM_NO, C.DEBIT_NOTE_NO, C.DATE_RTN, C.STORE, C.CC_SUPPL, C.CC_SUPPL_CONTRACT, C.CC_CRNCY_CD, C.CC_ITEM_GROUP, C.CC_SVAT_RATE, C.CC_VAT_RATE, D.SHORT_SKU, D.CC_MARGIN, D.CC_SEQ ";
//        strQueryAll = strQueryA;
//        if(SYSB2BPoAdjConcess != null && SYSB2BPoAdjConcess.equals("Y"))
//            strQueryAll = strQueryAll + strUNION + strQueryB;
//        strQueryAll = strQueryAll + strORDER_BY;

//          strQueryAll += "SELECT SUPPL, CONTRACT, REC.STORE, REC.DEPT,SEQ, " +
//                         "DOCUMENT_NO,STRMST.STORE_NAME, " +
//                         "REC.SHORT_SKU, REC.ITEM_DESC, CLASSMST.CLASS_DESC, SUBCLASS, " +
//                         "CASE  WHEN GROUPING(REC.STORE) = 1 THEN 'GRAND TOTAL'  " +
//                         "WHEN GROUPING (TYPE) = 1 THEN 'DOCUMENT TOTAL'  " +
//                         "ELSE TYPE END TYPE, " +
//                         "QTY, SUM(EXT_SELL_WOVAT) EXT_SELL_WOVAT, SVAT_RATE, SUM(VAT_AMT) VAT_AMT,  " +
//                         "INVOICE_NO, MARGIN, SUM(TI_COST_WOVAT) TI_COST_WOVAT, " + 
//                         "SUM(TI_COMM_COST_WOVAT) TI_COMM_COST_WOVAT, " +
//                         "TI_COMM_RATE, SUM(TI_COMM_VAT_AMT) TI_COMM_VAT_AMT,  " +
//                         "SUM(TI_COMM_COST_WVAT) TI_COMM_COST_WVAT, " +
//                         //supplier tax invoice to aeon
//                         "SUM(SUPP_COST_WOVAT) SUPP_COST_WOVAT, SUPPL_VAT_RATE,  " +
//                         "SUM(SUPPL_VAT_AMT) SUPPL_VAT_AMT, SUM(SUPP_COST_WVAT) SUPP_COST_WVAT, " +
//                         //aeon debit note to supplier
//                         "DN_INVOICE_NO,SUM(DN_COST_WOVAT) DN_COST_WOVAT,  " +
//                         "DN_RATE, SUM(DN_VAT_AMT) DN_VAT_AMT, SUM(DN_COST_WVAT) DN_COST_WVAT, " +
//                         //cn to supplier
//                         "CN_INVOICE_NO, CN_MARGIN, SUM(CN_COST_WOVAT) CN_COST_WOVAT,  " +
//                         "SUM(CN_CMM_COST_WOVAT) CN_CMM_COST_WOVAT, CN_CMM_RATE,  " +
//                         "SUM(CN_CMM_VAT_AMT) CN_CMM_VAT_AMT, SUM(CN_CMM_COST_WVAT) CN_CMM_COST_WVAT " +
//                         "FROM " +
//                          "( " +
//                         "SELECT '1' SEQ, CM_SUPPL SUPPL, CM_SUPPL_CONTRACT CONTRACT,  " +
//                         "H.DATE_COMMISSION UPDATE_DATE, H.DOCUMENT_NO, D.STORE, D.SHORT_SKU,  " +
//                         "I.ITEM_DESC, CM_ITEM_GROUP DEPT, I.SUBCLASS, 'PO' TYPE, " +
//                         "QTY_CM QTY, PD.EXT_SELL_ORD_WOVAT EXT_SELL_WOVAT,  " +
//                         "PH.PO_RTL_VAT_RATE SVAT_RATE,  " +
//                         "PD.EXT_SELL_ORD-PD.EXT_SELL_ORD_WOVAT VAT_AMT,  " +
//                         //aeon tax invoice to supplier
//                         "H.INVOICE_NO, D.CM_MARGIN MARGIN,  " +
//                         "(PD.ITEM_COST_ORD-D.EXT_COST_CM) TI_COST_WOVAT,  " +
//                          "D.EXT_COST_CM TI_COMM_COST_WOVAT, D.CM_VAT_RATE TI_COMM_RATE,  " +
//                         "(D.EXT_COST_CM_WVAT - EXT_COST_CM) TI_COMM_VAT_AMT,  " +
//                         "EXT_COST_CM_WVAT TI_COMM_COST_WVAT, " +
//                         //supplier tax invoice to aeon
//                         "PD.ITEM_COST_ORD SUPP_COST_WOVAT, PH.PO_VAT_RATE SUPPL_VAT_RATE ,  " +
//                         "(PD.EXT_COST_ORD_WVAT-PD.ITEM_COST_ORD) SUPPL_VAT_AMT,  " +
//                         "PD.EXT_COST_ORD_WVAT SUPP_COST_WVAT, " +
//                         //aeon debit note to supplier
//                         " '' DN_INVOICE_NO,0 DN_COST_WOVAT, 0 DN_RATE, 0 DN_VAT_AMT, 0 DN_COST_WVAT, " +
//
//                         //cn to supplier
//                         " '' CN_INVOICE_NO, 0 CN_MARGIN, 0 CN_COST_WOVAT, 0 CN_CMM_COST_WOVAT,  " +
//
//                         "0 CN_CMM_RATE, 0 CN_CMM_VAT_AMT, 0 CN_CMM_COST_WVAT " +
//                         "FROM CMHDR H, CMDET D, ITEMMST I, POHDR PH, PODET PD " +
//                         "WHERE D.COY = H.COY  " +
//                         "AND D.COY_SUB = H.COY_SUB  " +
//                         "AND D.STORE = H.STORE  " +
//                         "AND D.DOCUMENT_NO = H.DOCUMENT_NO  " +
//                         "AND D.CM_RSN_CD = H.CM_RSN_CD  " +
//                         "AND D.CM_NO = H.CM_NO  " +
//                         "AND D.SHORT_SKU = I.SHORT_SKU " +
//                         "AND PH.COY = PD.COY " +
//                         "AND PH.ORD_NO = PD.ORD_NO " +
//                         "AND PH.ORD_NO = D.DOCUMENT_NO " +
//                         "AND PD.PO_SEQ = D.CM_SEQ " +
//                         "AND PD.SHORT_SKU = D.SHORT_SKU " +
//                         "AND H.CREATE_METHOD = 'A' " +
//                         "AND H.CM_RSN_CD = 'CMPO' " +
//                         "AND H.CM_STATUS = 'U' " +
//                         "AND H.COY = '" + USER_COY  + "'" +
//                         "AND H.COY_SUB = '" + COY_SUB + "'" ;
//
//          if(SUPPL != null && SUPPL.length() > 0)
//          {
//            strQueryAll += "AND CM_SUPPL = '" + SUPPL + "'" ;
//          }
//          
//          if(SUPPL_CONT != null && SUPPL_CONT.length() > 0)
//          {
//            strQueryAll += "AND CM_SUPPL_CONTRACT = '" + SUPPL_CONT + "'" ;
//          }
//          
//          strQueryAll += "AND H.DATE_COMMISSION BETWEEN TO_DATE('" + first_day + "','yyyy-MM-dd')  AND  TO_DATE('" + last_day + "','yyyy-MM-dd') " +
//                         "UNION ALL " +
//                         "SELECT '2' SEQ, REBATE_SUPPL SUPPL, REBATE_CONTRACT CONTRACT,  " +
//                         "REBATE_UPDATE_DATE UPDATE_DATE, R.DEBIT_NOTE_NO DOCUMENT_NO,  " +
//                         "R.GUI_STORE STORE,'' SHORT_SKU, '' ITEM_DESC, REBATE_ITEM_GROUP DEPT,  " +
//                         "'' SUBCLASS, REBATE_TRANS_TYPE TYPE, 0 QTY, 0 EXT_SELL_WOVAT,  " +
//                         "0 SVAT_RATE, 0 VAT_AMT,  " +
//                         //aeon tax invoice to supplier
//                         "T.INVOICE_NO, 0 MARGIN, 0 TI_COST_WOVAT,  " +
//                         "T.INV_AMT_WOVAT TI_COMM_COST_WOVAT, T.INV_VAT_RATE TI_COMM_RATE,  " +
//                         "T.INV_VAT_AMOUNT TI_COMM_VAT_AMT, T.INV_AMT_WVAT TI_COMM_COST_WVAT, " +
//                         //supplier tax invoice to aeon
//                         "0 SUPP_COST_WOVAT, 0 SUPPL_VAT_RATE , 0 SUPPL_VAT_AMT, 0 SUPP_COST_WVAT, " +
//                         //aeon debit note to supplier
//                         " '' DN_INVOICE_NO,0 DN_COST_WOVAT, 0 DN_RATE, 0 DN_VAT_AMT, 0 DN_COST_WVAT, " +
//
//                         //cn to supplier
//                         " '' CN_INVOICE_NO, 0 CN_MARGIN, 0 CN_COST_WOVAT, 0 CN_CMM_COST_WOVAT,  " +
//
//                         "0 CN_CMM_RATE, 0 CN_CMM_VAT_AMT, 0 CN_CMM_COST_WVAT " +
//                         "FROM REBINV R, TAXINV T " +
//                         "WHERE REBATE_TRANS_TYPE IN  (" + SYSCSSMGSReason + ")" + 
//                         "AND REBATE_STATUS = 'U' " +
//                         "AND T.INV_STATUS = 'U' " +
//                         "AND R.DEBIT_NOTE_NO = T.DOCUMENT_NO  " +
//                         "AND R.COY = '" + USER_COY  + "'" +
//                         "AND R.COY_SUB = '" + COY_SUB  + "'" ;
//                         
//          if(SUPPL != null && SUPPL.length() > 0)
//          {
//            strQueryAll += "AND REBATE_SUPPL = '" + SUPPL + "'" ;
//          }
//                         
//          if(SUPPL_CONT != null && SUPPL_CONT.length() > 0)
//          {
//            strQueryAll += "AND REBATE_CONTRACT = '" + SUPPL_CONT  + "'" ;
//          }
//          
//          strQueryAll += "AND REBATE_UPDATE_DATE BETWEEN TO_DATE('" + first_day + "','yyyy-MM-dd')  AND  TO_DATE('" + last_day + "','yyyy-MM-dd') " +
//                         "UNION ALL  " +
//                         "SELECT '3' SEQ,  " +
//                         "CM_SUPPL SUPPL, CM_SUPPL_CONTRACT CONTRACT, H.DATE_COMMISSION UPDATE_DATE,  " +
//                         "H.DOCUMENT_NO, D.STORE,D.SHORT_SKU, I.ITEM_DESC, CM_ITEM_GROUP DEPT,   " +
//                         "I.SUBCLASS, 'GRN' TYPE,  " +
//                         "QTY_CM QTY, CD.EXT_COST_RTN*(-1) EXT_SELL_WOVAT, CH.CC_SVAT_RATE SVAT_RATE,  " +
//                         "(CD.EXT_COST_RTN_WVAT - CD.EXT_COST_RTN)*(-1) VAT_AMT,   " +
//                         //aeon tax invoice to supplier
//                         "'' INVOICE_NO, 0 MARGIN, 0 TI_COST_WOVAT, 0 TI_COMM_COST_WOVAT,   " +
//                         "0 TI_COMM_RATE, 0 TI_COMM_VAT_AMT, 0 TI_COMM_COST_WVAT,  " +
//                         //supplier tax invoice to aeon
//                         "0 SUPP_COST_WOVAT, 0 SUPPL_VAT_RATE, 0 SUPPL_VAT_AMT, 0 SUPP_COST_WVAT,  " +
//                         //aeon debit note to supplier
//                         "CH.DEBIT_NOTE_NO DN_INVOICE_NO, CD.EXT_COST_RTN DN_COST_WOVAT,   " +
//                         "CD.CC_VAT_RATE DN_RATE, CD.CC_VAT_AMT DN_VAT_AMT,   " +
//                         "CD.EXT_COST_RTN_WVAT DN_COST_WVAT,  " +
//                         //cn to supplier
//                         "H.INVOICE_NO CN_INVOICE_NO, D.CM_MARGIN CN_MARGIN,   " +
//                         "(CD.EXT_COST_RTN-D.EXT_COST_CM*(-1)) CN_COST_WOVAT,   " +
//                         "D.EXT_COST_CM*(-1) CN_CMM_COST_WOVAT, D.CM_VAT_RATE CN_CMM_RATE,   " +
//                         "(D.EXT_COST_CM_WVAT - EXT_COST_CM)*(-1) CN_CMM_VAT_AMT,   " +
//                         "EXT_COST_CM_WVAT*(-1) CN_CMM_COST_WVAT  " +
//                         "FROM CMHDR H, CMDET D, ITEMMST I, CCTXHDR CH, CCTXDET CD  " +
//                         "WHERE D.COY = H.COY   " +
//                         "AND D.COY_SUB = H.COY_SUB   " +
//                         "AND D.STORE = H.STORE   " +
//                         "AND D.DOCUMENT_NO = H.DOCUMENT_NO   " +
//                         "AND D.CM_RSN_CD = H.CM_RSN_CD   " +
//                         "AND D.CM_NO = H.CM_NO   " +
//                         "AND D.SHORT_SKU = I.SHORT_SKU  " +
//                         "AND CH.STORE = CD.STORE  " +
//                         "AND CH.CRED_CLAIM_NO = CD.CRED_CLAIM_NO  " +
//                         "AND CH.CRED_CLAIM_NO = D.DOCUMENT_NO  " +
//                         "AND CH.STORE = H.STORE   " +
//                         "AND CD.CC_SEQ = D.CM_SEQ  " +
//                         "AND CD.SHORT_SKU = D.SHORT_SKU  " +
//                         "AND CH.CC_STATUS = 'C'  " +
//                         "AND CH.CC_RSN_CD IN (" + SYSCSSGRNReason + ")" +
//                         "AND H.CREATE_METHOD IN ('A')  " +
//                         "AND H.CM_RSN_CD = 'CMCC'  " +
//                         "AND H.CM_STATUS = 'U'  " +
//                         "AND H.COY = '" + USER_COY + "'" +
//                         "AND H.COY_SUB = '" + COY_SUB + "'" ;
//
//          if(SUPPL != null && SUPPL.length() > 0)
//          {
//            strQueryAll += "AND CM_SUPPL = '" + SUPPL + "'" ;
//          }
//                         
//          if(SUPPL_CONT != null && SUPPL_CONT.length() > 0)
//          {
//            strQueryAll += "AND CM_SUPPL_CONTRACT = '" + SUPPL_CONT + "'" ;
//          }
//          strQueryAll += "AND H.DATE_COMMISSION BETWEEN TO_DATE('" + first_day + "','yyyy-MM-dd')  AND  TO_DATE('" + last_day + "','yyyy-MM-dd') " +
//                         "UNION ALL  " +
//                         "SELECT '4' SEQ, CM_SUPPL SUPPL, CM_SUPPL_CONTRACT CONTRACT,  " +
//                         "H.DATE_COMMISSION UPDATE_DATE, H.DOCUMENT_NO, D.STORE, " +
//                         "D.SHORT_SKU, I.ITEM_DESC,CM_ITEM_GROUP DEPT, I.SUBCLASS,  " +
//                         "'SRADJ' TYPE, 0 QTY, 0 EXT_SELL_WOVAT, 0 SVAT_RATE,0 VAT_AMT,  " +
//                         //aeon tax invoice to supplier
//                         "CASE WHEN H.CM_RSN_CD = 'CVPO' THEN H.INVOICE_NO ELSE '' END INVOICE_NO,  " +
//                         "0 MARGIN, 0 TI_COST_WOVAT,  " +
//                         "CASE WHEN H.CM_RSN_CD = 'CVPO' THEN D.EXT_COST_CM ELSE 0 END TI_COMM_COST_WOVAT,  " +
//                         "CASE WHEN H.CM_RSN_CD = 'CVPO' THEN D.CM_VAT_RATE ELSE 0 END TI_COMM_RATE,  " +
//                         "CASE WHEN H.CM_RSN_CD = 'CVPO' THEN (D.EXT_COST_CM_WVAT-EXT_COST_CM) ELSE 0 END TI_COMM_VAT_AMT,  " +
//                         "CASE WHEN H.CM_RSN_CD = 'CVPO' THEN EXT_COST_CM_WVAT ELSE 0 END TI_COMM_COST_WVAT, " +
//                         //supplier tax invoice to aeon
//                         "0 SUPP_COST_WOVAT, 0 SUPPL_VAT_RATE, 0 SUPPL_VAT_AMT, 0 SUPP_COST_WVAT, " +
//                         //aeon debit note to supplier
//                         "''  DN_INVOICE_NO, 0 DN_COST_WOVAT, 0 DN_RATE, 0 DN_VAT_AMT, 0 DN_COST_WVAT, " +
//                         //cn to supplier
//                         "CASE WHEN H.CM_RSN_CD = 'CVCC' THEN H.INVOICE_NO ELSE '' END CN_INVOICE_NO,  " +
//                         "0 CN_MARGIN, 0 CN_COST_WOVAT,  " +
//                         "CASE WHEN H.CM_RSN_CD = 'CVCC' THEN D.EXT_COST_CM*(-1) ELSE 0 END CN_CMM_COST_WOVAT,  " +
//                         "CASE WHEN H.CM_RSN_CD = 'CVCC' THEN D.CM_VAT_RATE ELSE 0 END CN_CMM_RATE,  " +
//                         "CASE WHEN H.CM_RSN_CD = 'CVCC' THEN (D.EXT_COST_CM_WVAT-EXT_COST_CM)*(-1) ELSE 0 END CN_CMM_VAT_AMT,  " +
//                         "CASE WHEN H.CM_RSN_CD = 'CVCC' THEN EXT_COST_CM_WVAT*(-1) ELSE 0 END CN_CMM_COST_WVAT " +
//                         "FROM CMHDR H, CMDET D, ITEMMST I " +
//                         "WHERE D.COY = H.COY  " +
//                         "AND D.COY_SUB = H.COY_SUB  " +
//                         "AND D.STORE = H.STORE  " +
//                         "AND D.DOCUMENT_NO = H.DOCUMENT_NO  " +
//                         "AND D.CM_RSN_CD = H.CM_RSN_CD  " +
//                         "AND D.CM_NO = H.CM_NO  " +
//                         "AND D.SHORT_SKU = I.SHORT_SKU " +
//                         "AND H.CREATE_METHOD IN ('A') " +
//                         "AND H.CM_RSN_CD IN (" + SYSCSSVarianceReason + ")" +
//                         "AND H.CM_STATUS = 'U' " +
//                         "AND H.COY = '" + USER_COY + "'" +
//                         "AND H.COY_SUB = '" + COY_SUB + "'" ;
//                      
//          if(SUPPL != null && SUPPL.length() > 0)
//          {
//            strQueryAll += "AND CM_SUPPL = '" + SUPPL + "'" ;
//          }
//                         
//          if(SUPPL_CONT != null && SUPPL_CONT.length() > 0)
//          {
//            strQueryAll += "AND CM_SUPPL_CONTRACT = '" + SUPPL_CONT + "'" ;
//          }
//          
//          strQueryAll += "AND H.DATE_COMMISSION BETWEEN TO_DATE('" + first_day + "','yyyy-MM-dd')  AND  TO_DATE('" + last_day + "','yyyy-MM-dd') " +
//                         "UNION ALL " +
//                         //PO Adjustment
//                         "SELECT '5' SEQ, P.PO_SUPPL SUPPL, P.PO_SUPPL_CONTRACT CONTRACT, P.DATE_ORDER UPDATE_DATE, " +
//                         "P.ORD_NO DOCUMENT_NO, P.PO_STORE STORE,D.SHORT_SKU, I.ITEM_DESC,PO_GROUP DEPT, I.SUBCLASS,  " +
//                         "'POADJ' TYPE, 0 QTY, 0 EXT_SELL_WOVAT, 0 SVAT_RATE,0 VAT_AMT,  " +
//                         //aeon tax invoice to supplier
//                         "'' INVOICE_NO, 0 MARGIN, 0 TI_COST_WOVAT, 0 TI_COMM_COST_WOVAT, 0 TI_COMM_RATE,  " +
//                         "0 TI_COMM_VAT_AMT, 0 TI_COMM_COST_WVAT, " +
//                         //supplier tax invoice to aeon
//                         "D.ITEM_COST_ORD SUPP_COST_WOVAT, P.PO_VAT_RATE SUPPL_VAT_RATE ,  " +
//                         "(D.EXT_COST_ORD_WVAT-D.ITEM_COST_ORD) SUPPL_VAT_AMT,  " +
//                         "D.EXT_COST_ORD_WVAT SUPP_COST_WVAT, " +
//                         //aeon debit note to supplier
//                         "'' DN_INVOICE_NO,0 DN_COST_WOVAT, 0 DN_RATE, 0 DN_VAT_AMT, 0 DN_COST_WVAT, " +
//                         //cn to supplier
//                         "'' CN_INVOICE_NO, 0 CN_MARGIN, 0 CN_COST_WOVAT, 0 CN_CMM_COST_WOVAT,  " +
//                         "0 CN_CMM_RATE, 0 CN_CMM_VAT_AMT, 0 CN_CMM_COST_WVAT " +
//                         "FROM 	POHDR P, PODET D, RECHDR R, POADJCHKLSTHDR A, VNCNTERM VN, ITEMMST I " +
//                         "WHERE P.COY = R.COY " +
//                         "AND P.ORD_NO = R.DOCUMENT_NO " +
//                         "AND A.COY = P.COY " +
//                         "AND A.COY_SUB = P.COY_SUB " +
//                         "AND A.CHKLST_STORE = P.PO_STORE " +
//                         "AND A.ADJ_ORD_NO = P.ORD_NO " +
//                         "AND A.CHKLST_STATUS = 'R' " +
//                         "AND VN.VN_CODE = P.PO_SUPPL " +
//                         "AND VN.VN_CONTRACT = P.PO_SUPPL_CONTRACT " +
//                         "AND VN.VN_SUBTYPE = 'C' " +
//                         "AND D.SHORT_SKU = I.SHORT_SKU " +
//                         "AND D.COY = P.COY " +
//                         "AND D.ORD_NO = P.ORD_NO " +
//                         "AND P.COY = '" + USER_COY + "'" +
//                         "AND P.COY_SUB = '" + COY_SUB  + "'" ;
//                         
//          if(SUPPL != null && SUPPL.length() > 0)
//          {
//            strQueryAll += "AND P.PO_SUPPL = '" + SUPPL + "'" ;
//          }
//                         
//          if(SUPPL_CONT != null && SUPPL_CONT.length() > 0)
//          {
//            strQueryAll += "AND P.PO_SUPPL_CONTRACT = '" + SUPPL_CONT + "'" ;
//          }
//          
//          strQueryAll += "AND P.DATE_ORDER BETWEEN TO_DATE('" + first_day + "','yyyy-MM-dd')  AND  TO_DATE('" + last_day + "','yyyy-MM-dd') " +
//                         "UNION ALL " +
//                         "SELECT '6' SEQ, CC_SUPPL SUPPL, CC_SUPPL_CONTRACT CONTRACT, CH.DATE_CONFIRM UPDATE_DATE, " +
//                         "TO_CHAR(CH.CRED_CLAIM_NO) DOCUMENT_NO, CH.STORE,CD.SHORT_SKU, I.ITEM_DESC, " +
//                         "CC_ITEM_GROUP DEPT, I.SUBCLASS,  " +
//                         "'GRNADJ' TYPE, 0 QTY, 0 EXT_SELL_WOVAT, 0 SVAT_RATE,0 VAT_AMT,  " +
//                         //aeon tax invoice to supplier
//                         "'' INVOICE_NO, 0 MARGIN, 0 TI_COST_WOVAT, 0 TI_COMM_COST_WOVAT,  " +
//                         "0 TI_COMM_RATE, 0 TI_COMM_VAT_AMT, 0 TI_COMM_COST_WVAT, " +
//                         //supplier tax invoice to aeon
//                         "0 SUPP_COST_WOVAT, 0 SUPPL_VAT_RATE, 0 SUPPL_VAT_AMT, 0 SUPP_COST_WVAT, " +
//                         //aeon debit note to supplier
//                         "CH.DEBIT_NOTE_NO DN_INVOICE_NO, CD.EXT_COST_RTN DN_COST_WOVAT,  " +
//                         "CD.CC_VAT_RATE DN_RATE, CD.CC_VAT_AMT DN_VAT_AMT,  " +
//                         "cd.EXT_COST_RTN_WVAT  DN_COST_WVAT, " +
//                         //cn to supplier
//                         "'' CN_INVOICE_NO, 0 CN_MARGIN, 0 CN_COST_WOVAT, 0 CN_CMM_COST_WOVAT,  " +
//                         "0 CN_CMM_RATE, 0 CN_CMM_VAT_AMT, 0 CN_CMM_COST_WVAT " +
//                         "from itemmst i, cctxhdr ch, cctxdet cd, VNCNTERM VN " +
//                         "where CH.STORE = CD.STORE " +
//                         "AND CH.CRED_CLAIM_NO = CD.CRED_CLAIM_NO " +
//                         "AND CD.SHORT_SKU = i.SHORT_SKU " +
//                         "AND VN.VN_CODE = CH.CC_SUPPL " +
//                         "AND VN.VN_CONTRACT = CH.CC_SUPPL_CONTRACT " +
//                         "AND VN.VN_SUBTYPE = 'C' " +
//                         "AND CH.CC_STATUS = 'C' " +
//                         "AND CH.CC_CREATION_METHOD = 'ADJ' " +
//                         "AND CH.CC_RSN_CD not in (" + SYSCSSGRNADJReason + ")" +
//                         "AND CH.COY = '" + USER_COY + "'" +
//                         "AND CH.COY_SUB = '" + COY_SUB + "'" ;
//                         
//          if(SUPPL != null && SUPPL.length() > 0)
//          {
//            strQueryAll += "AND CC_SUPPL = '" + SUPPL + "'" ;
//          }
//                         
//          if(SUPPL_CONT != null && SUPPL_CONT.length() > 0)
//          {
//            strQueryAll += "AND CC_SUPPL_CONTRACT = '" + SUPPL_CONT + "'" ;
//          }
//          
//          strQueryAll += "AND CH.DATE_CONFIRM BETWEEN TO_DATE('" + first_day + "','yyyy-MM-dd')  AND  TO_DATE('" + last_day + "','yyyy-MM-dd') " +
//                         "UNION ALL " +
//                         "SELECT '7' SEQ, CM_SUPPL SUPPL, CM_SUPPL_CONTRACT CONTRACT,  " +
//                         "H.DATE_COMMISSION UPDATE_DATE, H.DOCUMENT_NO, D.STORE, D.SHORT_SKU,  " +
//                         "I.ITEM_DESC, CM_ITEM_GROUP DEPT, I.SUBCLASS, 'MARADJ' TYPE, " +
//                         "0 QTY, 0 EXT_SELL_WOVAT, 0 SVAT_RATE,0 VAT_AMT,  " +
//                         //aeon tax invoice to supplier
//                         "CASE WHEN H.CM_RSN_CD = 'CMPO' THEN H.INVOICE_NO ELSE '' END INVOICE_NO,  " +
//                         "0 MARGIN, 0 TI_COST_WOVAT,  " +
//                         "CASE WHEN H.CM_RSN_CD = 'CMPO' THEN D.EXT_COST_CM ELSE 0 END TI_COMM_COST_WOVAT,  " +
//                         "CASE WHEN H.CM_RSN_CD = 'CMPO' THEN D.CM_VAT_RATE ELSE 0 END TI_COMM_RATE,  " +
//                         "CASE WHEN H.CM_RSN_CD = 'CMPO' THEN (D.EXT_COST_CM_WVAT-EXT_COST_CM) ELSE 0 END TI_COMM_VAT_AMT,  " +
//                         "CASE WHEN H.CM_RSN_CD = 'CMPO' THEN EXT_COST_CM_WVAT ELSE 0 END TI_COMM_COST_WVAT, " +
//                         //supplier tax invoice to aeon
//                         " 0 SUPP_COST_WOVAT, 0 SUPPL_VAT_RATE, 0 SUPPL_VAT_AMT, 0 SUPP_COST_WVAT," +
//                         //aeon debit note to supplier
//                         " ''  DN_INVOICE_NO, 0 DN_COST_WOVAT, 0 DN_RATE, 0 DN_VAT_AMT, 0 DN_COST_WVAT," +
//                         //cn to supplier
//                         " CASE WHEN H.CM_RSN_CD = 'CMCC' THEN H.INVOICE_NO ELSE '' END CN_INVOICE_NO," +
//                         " 0 CN_MARGIN, 0 CN_COST_WOVAT,  " +
//                         " CASE WHEN H.CM_RSN_CD = 'CMCC' THEN D.EXT_COST_CM*(-1) ELSE 0 END CN_CMM_COST_WOVAT," +
//                         " CASE WHEN H.CM_RSN_CD = 'CMCC' THEN D.CM_VAT_RATE ELSE 0 END CN_CMM_RATE," +
//                         " CASE WHEN H.CM_RSN_CD = 'CMCC' THEN (D.EXT_COST_CM_WVAT-EXT_COST_CM)*(-1) ELSE 0 END CN_CMM_VAT_AMT," +
//                         " CASE WHEN H.CM_RSN_CD = 'CMCC' THEN EXT_COST_CM_WVAT*(-1) ELSE 0 END CN_CMM_COST_WVAT" +
//                         " FROM CMHDR H, CMDET D, ITEMMST I" +
//                         " WHERE D.COY = H.COY " +
//                         " AND D.COY_SUB = H.COY_SUB " +
//                         " AND D.STORE = H.STORE " +
//                         " AND D.DOCUMENT_NO = H.DOCUMENT_NO " +
//                         " AND D.CM_RSN_CD = H.CM_RSN_CD" +
//                         " AND D.CM_NO = H.CM_NO" +
//                         " AND D.SHORT_SKU = I.SHORT_SKU" +
//                         " AND H.CREATE_METHOD = 'M'" +
//                         " AND H.CM_RSN_CD IN ('CMPO','CMCC')" +
//                         " AND H.CM_STATUS = 'U'" +
//                         " AND H.COY = '" + USER_COY + "'" +
//                         " AND H.COY_SUB = '" + COY_SUB + "'" ;
//                      
//          if(SUPPL != null && SUPPL.length() > 0)
//          {
//            strQueryAll += "AND CM_SUPPL = '" + SUPPL + "'" ;
//          }
//                         
//          if(SUPPL_CONT != null && SUPPL_CONT.length() > 0)
//          {
//            strQueryAll += " AND CM_SUPPL_CONTRACT = '" + SUPPL_CONT + "'" ;
//          }
//          
//          strQueryAll += " AND H.DATE_COMMISSION BETWEEN TO_DATE('" + first_day + "','yyyy-MM-dd')  AND  TO_DATE('" + last_day + "','yyyy-MM-dd') " +
//                         ") REC, STRMST, CLASSMST" +
//                         " WHERE REC.STORE = STRMST.STORE" +
//                         " AND REC.DEPT = CLASSMST.CLASS" +
//                         //AND DOCUMENT_NO IN ('10011000005513','10011000005514','10011000005515')
//                         " GROUP BY  GROUPING SETS ((SUPPL, CONTRACT, DOCUMENT_NO,REC.STORE," +
//                         " STRMST.STORE_NAME, REC.SHORT_SKU, REC.ITEM_DESC, REC.DEPT, CLASSMST.CLASS_DESC," +
//                         " SUBCLASS, TYPE,SEQ, QTY, SVAT_RATE, INVOICE_NO, MARGIN, TI_COMM_RATE," +
//                         " SUPPL_VAT_RATE, DN_INVOICE_NO, DN_RATE, CN_INVOICE_NO, CN_MARGIN, CN_CMM_RATE)," +
//                         " (SUPPL, CONTRACT, SEQ, DOCUMENT_NO,REC.STORE, REC.DEPT)," +
//                         " (SUPPL, CONTRACT))" +
//                         " ORDER BY SUPPL, CONTRACT, REC.STORE, SEQ, REC.DEPT, DOCUMENT_NO, SHORT_SKU" ;
        strQueryAll += "SELECT  SUPPL, CONTRACT, REC.PROCESSED_DATE, sales_start_date, sales_end_date, " ;
        strQueryAll += " REC.STORE, REC.DEPT,SEQ, REC.DOCUMENT_NO,S.STORE_NAME," ;
        
        //AU-SD011432058 
        strQueryAll += " REC.SHORT_SKU, (SELECT I.ITEM_DESC FROM ITEMMST I WHERE REC.SHORT_SKU = I.SHORT_SKU) ITEM_DESC, C.CLASS_DESC, REC.SUBCLASS," ;
        
        strQueryAll += " CASE  WHEN GROUPING(REC.STORE) = 1 THEN 'GRAND TOTAL'" ;
        strQueryAll += " WHEN GROUPING (TYPE) = 1 THEN 'DOCUMENT TOTAL'" ;
        strQueryAll += " ELSE TYPE END TYPE," ;
        strQueryAll += " QTY, SUM(EXT_SELL_WOVAT) EXT_SELL_WOVAT, SVAT_RATE, SUM(VAT_AMT) VAT_AMT," ;
        strQueryAll += " INVOICE_NO, MARGIN, SUM(TI_COST_WOVAT) TI_COST_WOVAT," ;
        strQueryAll += " SUM(TI_COMM_COST_WOVAT) TI_COMM_COST_WOVAT," ;
        strQueryAll += " TI_COMM_RATE, SUM(TI_COMM_VAT_AMT) TI_COMM_VAT_AMT," ;
        strQueryAll += " SUM(TI_COMM_COST_WVAT) TI_COMM_COST_WVAT," ;
        //--supplier tax invoice to aeon
        strQueryAll += " SUM(SUPP_COST_WOVAT) SUPP_COST_WOVAT, SUPPL_VAT_RATE," ;
        strQueryAll += " SUM(SUPPL_VAT_AMT) SUPPL_VAT_AMT, SUM(SUPP_COST_WVAT) SUPP_COST_WVAT," ;
        //--aeon debit note to supplier
        strQueryAll += " DN_INVOICE_NO,SUM(DN_COST_WOVAT) DN_COST_WOVAT," ;
        strQueryAll += " DN_RATE, SUM(DN_VAT_AMT) DN_VAT_AMT, SUM(DN_COST_WVAT) DN_COST_WVAT," ;
        //--cn to supplier
        strQueryAll += " CN_INVOICE_NO, CN_MARGIN, SUM(CN_COST_WOVAT) CN_COST_WOVAT," ;
        strQueryAll += " SUM(CN_CMM_COST_WOVAT) CN_CMM_COST_WOVAT, CN_CMM_RATE, " ;
        strQueryAll += " SUM(CN_CMM_VAT_AMT) CN_CMM_VAT_AMT, SUM(CN_CMM_COST_WVAT)" ;
        strQueryAll += " CN_CMM_COST_WVAT" ;
        strQueryAll += " FROM CSSDOC REC, STRMST S, CLASSMST C, VNMSTR V" ;
        strQueryAll += " WHERE" ;
        strQueryAll += " REC.STORE = S.STORE" ;
        strQueryAll += " AND REC.DEPT = C.CLASS" ;
        strQueryAll += " AND REC.SUPPL = V.VN_CODE" ;
        strQueryAll += " AND (VN_PO_SEND_MTHD NOT IN ('B','I','E') or REC.CSS_DOWNLOAD_FLAG = 'Y') " ;
        strQueryAll += " AND REC.COY = '" + USER_COY + "'" ;
        strQueryAll += " AND REC.COY_SUB = '" + COY_SUB + "'" ;
        strQueryAll += " AND REC.UPDATE_DATE BETWEEN TO_DATE('" + first_day + "','yyyy-MM-dd')  AND  TO_DATE('" + last_day + "','yyyy-MM-dd') " ;
        
        if(SUPPL != null && SUPPL.length() > 0)
        {
          strQueryAll += "AND REC.SUPPL = '" + SUPPL + "'" ; 
        }
                         
        if(SUPPL_CONT != null && SUPPL_CONT.length() > 0)
        {
          strQueryAll += " AND REC.CONTRACT = '" + SUPPL_CONT + "'" ;
        }

        strQueryAll += " GROUP BY GROUPING SETS ((REC.SUPPL, REC.CONTRACT, REC.PROCESSED_DATE," ;
        strQueryAll += " sales_start_date, sales_end_date, REC.DOCUMENT_NO,REC.STORE, " ;
        strQueryAll += " S.STORE_NAME, REC.SHORT_SKU, REC.DEPT, C.CLASS_DESC," ;
        strQueryAll += " REC.SUBCLASS, TYPE,SEQ, QTY, SVAT_RATE, INVOICE_NO, MARGIN, TI_COMM_RATE," ;
        strQueryAll += " SUPPL_VAT_RATE, DN_INVOICE_NO, DN_RATE, CN_INVOICE_NO, CN_MARGIN, CN_CMM_RATE, " ;
        strQueryAll += " REC.REC_SEQ)," ;
        strQueryAll += " (REC.SUPPL, REC.CONTRACT, REC.PROCESSED_DATE, sales_start_date, sales_end_date, SEQ," ;
        strQueryAll += " DOCUMENT_NO,REC.STORE, REC.DEPT), " ;
        strQueryAll += " (REC.SUPPL, REC.CONTRACT, REC.PROCESSED_DATE))" ;
        strQueryAll += " ORDER BY REC.SUPPL, REC.CONTRACT, REC.PROCESSED_DATE, REC.STORE, SEQ, REC.DEPT," ;
        strQueryAll += " REC.DOCUMENT_NO, REC.SHORT_SKU, REC.REC_SEQ" ;

        return strQueryAll;
    }

    public String getCommissionQuery()
        throws Exception
    {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append(" SELECT CH.CM_VAT_RATE, CD.EXT_COST_CM, CD.EXT_COST_CM_WVAT ");
        strBuff.append(" FROM CMHDR CH, CMDET CD ");
        strBuff.append(" WHERE CD.COY = ? AND CD.DOCUMENT_NO =? AND CD.SHORT_SKU = ? AND CD.CM_SEQ = ? ");
        strBuff.append(" AND CD.COY = CH.COY AND CD.DOCUMENT_NO = CH.DOCUMENT_NO AND CH.CM_RSN_CD = 'CMPO' ");
        return strBuff.toString();
    }

    public String getConcessHeaderQuery(String strDocNo, String strStore, String strSuppl, String strSupplCont)
        throws Exception
    {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SELECT DISTINCT C.FR_DATE, C.TO_DATE FROM   CSPURAONHDR C, CSPURAONDET D ");
        strBuff.append("WHERE C.PER=D.PER AND C.DATE_BATCH_NO=D.DATE_BATCH_NO AND C.COY=D.COY AND C.COY_SUB=D.COY_SUB AND C.STORE=D.STORE ");
        strBuff.append("AND C.PO_SUPPL=D.PO_SUPPL AND C.PO_SUPPL_CONTRACT=D.PO_SUPPL_CONTRACT AND C.ITEM_GROUP=D.ITEM_GROUP ");
        strBuff.append("AND C.VAT_CODE=D.VAT_CODE AND C.TYPE=D.TYPE AND C.SVAT_CODE=D.SVAT_CODE AND C.STAX_RATE=D.STAX_RATE ");
        strBuff.append("AND D.DOCUMENT_NO = '" + strDocNo + "' AND D.STORE = '" + strStore + "' ");
        strBuff.append("AND C.PO_SUPPL = '" + strSuppl + "' AND C.PO_SUPPL_CONTRACT = '" + strSupplCont + "' ");
        return strBuff.toString();
    }

    private String getProfitvvValue(String coy, String vnm)
        throws Exception
    {
        profitvvSQL.setVObject((new ProfitvvSQL()).getVObject());
        profitvvSQL.setCOY(coy);
        profitvvSQL.setVNM(vnm);
        profitvvSQL.getByKey();
        return profitvvSQL.VNM_VDTVL();
    }

    private Connection conn;
    private ResultSet resultSet;
    private PreparedStatement pstmt;
    private ProfitvvSQL profitvvSQL;
    private String SYSB2BPoAdjConcess;
    private String SYSCSSMGSReason ;
    private String SYSCSSGRNReason ;
    private String SYSCSSVarianceReason ;
    private String SYSCSSGRNADJReason ;
}