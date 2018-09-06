/**
 * Copyright (C) 2005 QR Retail Autotamation Sdn. Bhd.
 * All right reserved.

/**
 * Synopsis: PRINT DEBIT NOTE
 *
 * Written: Profit V7.0 Project Team: Peter
 *
 * Revised: Name/ Date.
 * Au Yong - 2010-05-24
 */

package qrcom.PROFIT.reports.CC;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import qrcom.PROFIT.reports.GenericPageEvent;
import qrcom.PROFIT.shared.constants.IReport;

import qrcom.util.HParam;
import qrcom.util.qrMisc;
import qrcom.PROFIT.files.info.*;
import qrcom.PROFIT.reports.GenericReport;
import qrcom.util.ejb.jdbc.support.Parameters;
import qrcom.util.ejb.jdbc.support.StatementManager;

public class ConsTaxInvRpt extends GenericReport {
    private boolean test_run = false;
    private String user_lang = null;
    private Statement stmt = null;

    private PreparedStatement psHdr = null;
    private PreparedStatement psDet = null;
    private ResultSet rsHdr = null;
    private ResultSet rsDet = null;

    private Font FontHeaderType = null;
    private Font FontTitleType = null;
    private Font FontDetailType = null;
    private Font FontTableType = null;
    private Font FontDataType = null;
    private Font FontTotalType = null;
    private Font FontFooterType = null;
    private Font FontPagesType = null;

    private SysctlSQL sysctlSQL = null;
    private TaxinvSQL taxinvSQL = null;
    private AdlangmstSQL adlangmstSQL = null;
    private ProfitvvSQL profitvvSQL = null;
    private CoymstSQL coymstSQL = null;
    private CoysubmstSQL coysubmstSQL = null;
    private StrmstSQL strmstSQL = null;
    //  private ICUNumberFormat formatter = null;

    private int intGlobalCount = 0;
    private int intMaxRecord = 15;
    private int intTotalRow = 0;
    private int intPageNum = 0;
    private int intPageTotal = 0;
    private int intTotalWrap = 1;
    private int intHdrDescSize = 40;
    private int intDataDescSize = 40;

    private double dbltotal = 0;

    private boolean printBlankPage = false;
    private boolean addHdrReportLine1 = false;
    private boolean addHdrReportLine2 = false;

    private Table tableSuperHdr = null;
    private Table tableSuperSuperHdr = null;
    private Table tableHdr = null;
    private Table tableSuperTableHdr = null;

    private GenericPageEvent rpt_header_footer = null;
    private SimpleDateFormat reportGenTime = new SimpleDateFormat("yyyy-MM-dd hh:mm aaa");

    private String strFooterMsg1 =
        "Discrepancy in this document should be notified to us within 3 days from the date of this document.";
    private String strFooterMsg2 = "Please note that the amount due will be offset against our next payment to you.";
    private String strFooterMsg3 = "This is a computer generated  invoice. No signatory is required.";

    private String strUSER_ID = "";
    private String strTOTAL_ROW = "";
    private String strPAGE_ID = "";
    private String strSTORE = "";
    private String strCOY = "";
    private String strREPORT_TYPE = "";
    private String strPRINT_ALL = "";
    private String strGlobalCoy = "";
    private String strGlobalCoy_sub = "";
    private String strDescription = "";
    private String strDateFormat = "dd/MM/yyyy";
    private String time = "";
    private String SYSCompanyLogo = "";
    private String SYSCompanyName = "";
    private String SYSCompanyRegNo = "";
    private String SYSTaxInvFormula = "";
    private String SYSTaxInType = "";
    private String SYSUserLanguage = "";
    private String SYSUserCountry = "";
    private String SYSDefaultLanguage = "";
    private String SYSLangCode = "";
    private String SYSInvLanguage = "";
    private String SYSReportLine = "";
    private String SYSRptFontTaxInv = "";
    private String SYSRptFontEncode2 = "";
    private String SYSInvStrNameLength = "";
    private String SYSCurrCom = "";
    private String SYSRptIdn = ""; //fadhilah 3/feb/16

    private String strPA_COY = "";
    private String strPA_COY_SUB = "";
    private String strPA_STORE = "";
    private String strPA_PROCESS_ID = "";
    private String strPA_INVOICE_NO = "";
    private String strPA_SUPPL = "";
    private String strPA_SUPPL_CONTRACT = "";
    private String strPA_ITEM_GROUP = "";
    private String strPA_DOCUMENT_NO = "";
    private String strPA_INV_DATE_FR = "";
    private String strPA_INV_DATE_TO = "";
    private String strPA_UPDATE_DATE_FR = "";
    private String strPA_UPDATE_DATE_TO = "";

    private int intSYSInvStrNameLength = 0;

    private String aryTaxinv[][] = new String[11][13];
    private String aryWordWrap[] = null;
    private String formula[] = null;

    private TaxrptctrlSQL taxrptctrlSQL = null;
    private static final String DOC_TYPE = "Concessionaire Tax Invoice";
    private String strReportFormat = "";

    private ConsTaxInvRpt() {
    }

    public ConsTaxInvRpt(String filename) {
        super(filename);
    }

    public ConsTaxInvRpt(OutputStream outStream) {
        super(outStream);
    }

    public void print(HParam hParam) {
        try {
            super.openOutputStream();
            openConnection();
            conn.setAutoCommit(false);

            initObjSQL();
            jInit(hParam);

            intPageTotal = 1;
            intPageNum = 1;

            if (strREPORT_TYPE.equals("CONCESS_INV")) {
                if (strPRINT_ALL != null && strPRINT_ALL.equals("TRUE")) {
                    printAllConcessTaxInvoice();
                } else {
                    for (int i = 0; i < intTotalRow; i++) {
                        printConcessTaxInvoice(i);
                    }
                }
            }

            conn.commit();
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection();
            super.closeOutputStream();
        }
    }

    private void initObjSQL() throws SQLException {
        sysctlSQL = new SysctlSQL(conn);
        taxinvSQL = new TaxinvSQL(conn);
        adlangmstSQL = new AdlangmstSQL(conn);
        profitvvSQL = new ProfitvvSQL(conn);
        coymstSQL = new CoymstSQL(conn);
        coysubmstSQL = new CoysubmstSQL(conn);
        strmstSQL = new StrmstSQL(conn);
        stmt = conn.createStatement();
        taxrptctrlSQL = new TaxrptctrlSQL(conn);
    }

    private void initTotal() throws Exception {
        dbltotal = 0;
    }

    private void jInit(HParam hParam) throws Exception {
        document = new Document(PageSize.A4); // Margin : left, right, top, bottom
        document.setMargins(30, 18, 45, 45);

        strUSER_ID = hParam.getString("USER_ID").trim();
        strTOTAL_ROW = hParam.getString("TOTAL_ROW").trim();
        strPAGE_ID = hParam.getString("PAGE_ID").trim();
        strREPORT_TYPE = hParam.getString("REPORT_TYPE").trim();
        strPRINT_ALL = hParam.getString("PRINT_ALL").trim();

        if (strTOTAL_ROW == null) {
            strTOTAL_ROW = "0";
        }

        if (strREPORT_TYPE == null) {
            strREPORT_TYPE = "";
        }

        intTotalRow = Integer.parseInt(strTOTAL_ROW);

        if (strPRINT_ALL != null && strPRINT_ALL.equals("TRUE")) {
            strPA_COY = hParam.getString("PA_COY").trim();
            strPA_COY_SUB = hParam.getString("PA_COY_SUB").trim();
            strPA_STORE = hParam.getString("PA_STORE").trim();
            strPA_PROCESS_ID = hParam.getString("PA_PROCESS_ID").trim();
            strPA_INVOICE_NO = hParam.getString("PA_INVOICE_NO").trim();
            strPA_SUPPL = hParam.getString("PA_SUPPL").trim();
            strPA_SUPPL_CONTRACT = hParam.getString("PA_SUPPL_CONTRACT").trim();
            strPA_ITEM_GROUP = hParam.getString("PA_ITEM_GROUP").trim();
            strPA_DOCUMENT_NO = hParam.getString("PA_DOCUMENT_NO").trim();
            strPA_INV_DATE_FR = hParam.getString("PA_INV_DATE_FR").trim();
            strPA_INV_DATE_TO = hParam.getString("PA_INV_DATE_TO").trim();
            strPA_UPDATE_DATE_FR = hParam.getString("PA_UPDATE_DATE_FR").trim();
            strPA_UPDATE_DATE_TO = hParam.getString("PA_UPDATE_DATE_TO").trim();
        } else {
            for (int i = 0; i < intTotalRow; i++) {
                aryTaxinv[i][0] = hParam.getString("COY_" + String.valueOf(i + 1)).trim();
                aryTaxinv[i][1] = hParam.getString("COY_SUB_" + String.valueOf(i + 1)).trim();
                aryTaxinv[i][2] = hParam.getString("STORE_" + String.valueOf(i + 1)).trim();
                aryTaxinv[i][3] = hParam.getString("PROCESS_ID_" + String.valueOf(i + 1)).trim();
                aryTaxinv[i][4] = hParam.getString("FORMS_" + String.valueOf(i + 1)).trim();
                aryTaxinv[i][5] = hParam.getString("SERIAL_NO_" + String.valueOf(i + 1)).trim();
                aryTaxinv[i][6] = hParam.getString("INVOICE_NO_" + String.valueOf(i + 1)).trim();
                aryTaxinv[i][7] = hParam.getString("DEBIT_NOTE_NO_" + String.valueOf(i + 1)).trim();
                aryTaxinv[i][8] = hParam.getString("CRED_CLAIM_NO_" + String.valueOf(i + 1)).trim();
                aryTaxinv[i][9] = hParam.getString("ADJ_NO_" + String.valueOf(i + 1)).trim();
                aryTaxinv[i][10] = hParam.getString("XFR_NO_" + String.valueOf(i + 1)).trim();
                aryTaxinv[i][11] = hParam.getString("DOCUMENT_NO_" + String.valueOf(i + 1)).trim();
                aryTaxinv[i][12] = hParam.getString("DOC_TYPE_" + String.valueOf(i + 1)).trim();
            }
        }

        strCOY = retrieveUserCoy(strUSER_ID);
        user_lang = retrieveUserLanguage(strUSER_ID);
        time = reportGenTime.format(sysctlSQL.getLAST_DAY_UPDATEDPlusOne());

        setGroupingForReport(strCOY);
        //    formatter = new ICUNumberFormat(currencyConverter);

        if (user_lang.trim().length() == 0) {
            user_lang = "0";
        }

        super.USER_LANGUAGE = user_lang;

        SYSCompanyLogo = getPROFITVV("SYSCompanyLogo");
        SYSCompanyName = getPROFITVV("SYSCompanyName");
        SYSCompanyRegNo = getPROFITVV("SYSCompanyRegNo");
        SYSTaxInvFormula = getPROFITVV("SYSTaxInvFormula");
        SYSTaxInType = getPROFITVV("SYSTaxInType");
        SYSUserLanguage = getPROFITVV("SYSUserLanguage");
        SYSUserCountry = getPROFITVV("SYSUserCountry");
        SYSDefaultLanguage = getPROFITVV("SYSDefaultLanguage");
        SYSLangCode = getPROFITVV("SYSLangCode");
        SYSInvLanguage = getPROFITVV("SYSInvLanguage");
        SYSReportLine = getPROFITVV("SYSReportLine");
        SYSRptFontTaxInv = getPROFITVV("SYSRptFontTaxInv");
        SYSRptFontEncode2 = getPROFITVV("SYSRptFontEncode2");
        SYSInvStrNameLength = getPROFITVV("SYSInvStrNameLength");
        SYSCurrCom = getPROFITVV("SYSCurrCom");
        SYSRptIdn = getPROFITVV("SYSRptIdn"); //fadhilah 3/feb/16

        intSYSInvStrNameLength = Integer.parseInt(SYSInvStrNameLength);

        BASE_FONT_Chinese = BaseFont.createFont(SYSRptFontTaxInv, SYSRptFontEncode2, BaseFont.NOT_EMBEDDED);

        FontChinese = new Font(BASE_FONT_Chinese, 10, Font.NORMAL);
        FontHeaderType = new Font(BASE_FONT_Chinese, 8, Font.NORMAL);
        FontTitleType = new Font(BASE_FONT_Chinese, 16, Font.BOLD);
        FontDetailType = new Font(BASE_FONT_Chinese, 9, Font.NORMAL);
        FontTableType = new Font(BASE_FONT_Chinese, 10, Font.BOLD);
        FontDataType = new Font(BASE_FONT_Chinese, 9, Font.NORMAL);
        FontTotalType = new Font(BASE_FONT_Chinese, 9, Font.BOLD);
        FontFooterType = new Font(BASE_FONT_Chinese, 8, Font.ITALIC);
        FontPagesType = new Font(BASE_FONT_Chinese, 8, Font.NORMAL);

        formula = SYSTaxInvFormula.split("");

        rpt_header_footer = new GenericPageEvent(conn, FontChinese);
        rpt_header_footer.setCoy(strCOY, user_lang, strUSER_ID);

        // Creation Of The Different Writers
        pdfwriter = PdfWriter.getInstance(document, outStream); // MUST Place This Line Right After The New Document(...
        // pdfwriter.setPageEvent(rpt_header_footer);

        document.open();
    }

    private String getPrintAllHeaderQuery() throws SQLException {
        String query =
            "SELECT T.*, " + "REPLACE(REPLACE(REPLACE((SELECT Translate(P.VNM_VDTVL, 'FSI', '!^?') " +
            "FROM PROFITVV P " +
            "WHERE VNM = 'SYSTaxInvFormula' AND P.COY = T.COY), '!', T.FORM), '^', T.SERIAL_NO), '?', T.INVOICE_NO) AS INV_NO " +
            "FROM TAXINV T ";

        String where = "WHERE T.DOC_TYPE = 'CM' " + "AND T.INV_STATUS = 'U' ";

        if (strPA_COY != null && strPA_COY.length() > 0) {
            where += "AND T.COY = '" + strPA_COY + "' ";
        }

        if (strPA_COY_SUB != null && strPA_COY_SUB.length() > 0) {
            where += "AND T.COY_SUB = '" + strPA_COY_SUB + "' ";
        }

        if (strPA_STORE != null && strPA_STORE.length() > 0) {
            where += "AND T.STORE = " + "'" + strPA_STORE + "' ";
        }

        if (strPA_PROCESS_ID != null && strPA_PROCESS_ID.length() > 0) {
            where += "AND T.PROCESS_ID = '" + strPA_PROCESS_ID + "' ";
        }

        if (strPA_INVOICE_NO != null && strPA_INVOICE_NO.length() > 0) {
            where += "AND T.INVOICE_NO = '" + strPA_INVOICE_NO + "' ";
        }

        if (strPA_SUPPL != null && strPA_SUPPL.length() > 0) {
            where += "AND T.SUPPL = '" + strPA_SUPPL + "' ";
        }

        if (strPA_SUPPL_CONTRACT != null && strPA_SUPPL_CONTRACT.length() > 0) {
            where += "AND T.SUPPL_CONTRACT = '" + strPA_SUPPL_CONTRACT + "' ";
        }

        if (strPA_ITEM_GROUP != null && strPA_ITEM_GROUP.length() > 0) {
            where += "AND T.ITEM_GROUP = '" + strPA_ITEM_GROUP + "' ";
        }

        if (strPA_DOCUMENT_NO != null && strPA_DOCUMENT_NO.length() > 0) {
            where += "AND T.DOCUMENT_NO = '" + strPA_DOCUMENT_NO + "' ";
        }

        if (strPA_INV_DATE_FR != null && strPA_INV_DATE_FR.length() > 0 && strPA_INV_DATE_TO != null &&
            strPA_INV_DATE_TO.length() > 0) {
            where +=
                "AND T.INV_DATE BETWEEN TO_DATE('" + strPA_INV_DATE_FR + "', 'YYYY-MM-DD') AND TO_DATE('" +
                strPA_INV_DATE_TO + "', 'YYYY-MM-DD') ";
        }

        if (strPA_UPDATE_DATE_FR != null && strPA_UPDATE_DATE_FR.length() > 0 && strPA_UPDATE_DATE_TO != null &&
            strPA_UPDATE_DATE_TO.length() > 0) {
            where +=
                "AND T.UPDATE_DATE BETWEEN TO_DATE('" + strPA_UPDATE_DATE_FR + "', 'YYYY-MM-DD') AND TO_DATE('" +
                strPA_UPDATE_DATE_TO + "', 'YYYY-MM-DD') ";
        }

        query = query + where + " ORDER BY T.STORE, T.INV_DATE DESC, T.PROCESS_ID ";

        return (query);
    }

    private String getHeaderQuery(int count) throws SQLException {        
        String query =
            "SELECT T.*, " + "REPLACE(REPLACE(REPLACE((SELECT Translate(P.VNM_VDTVL, 'FSI', '!^?') " +
            "FROM PROFITVV P " +
            "WHERE VNM = 'SYSTaxInvFormula' AND P.COY = T.COY), '!', T.FORM), '^', T.SERIAL_NO), '?', T.INVOICE_NO) AS INV_NO " +
            "FROM TAXINV T ";

        String where = "WHERE T.DOC_TYPE = 'CM' " + "AND T.INV_STATUS = 'U' ";

        if (aryTaxinv[count][0] != null && aryTaxinv[count][0].length() > 0) {
            where += "AND T.COY = '" + aryTaxinv[count][0] + "' ";
        }

        if (aryTaxinv[count][1] != null && aryTaxinv[count][1].length() > 0) {
            where += "AND T.COY_SUB = '" + aryTaxinv[count][1] + "' ";
        }

        if (aryTaxinv[count][2] != null && aryTaxinv[count][2].length() > 0) {
            where += "AND T.STORE = " + "'" + aryTaxinv[count][2] + "' ";
        }

        if (aryTaxinv[count][3] != null && aryTaxinv[count][3].length() > 0) {
            where += "AND T.PROCESS_ID = '" + aryTaxinv[count][3] + "' ";
        }

        if (aryTaxinv[count][7] != null && aryTaxinv[count][7].length() > 0) {
            where += "AND T.DEBIT_NOTE_NO = '" + aryTaxinv[count][7] + "' ";
        }

        if (aryTaxinv[count][8] != null && aryTaxinv[count][8].length() > 0) {
            where += "AND T.CRED_CLAIM_NO = " + aryTaxinv[count][8] + " ";
        }

        if (aryTaxinv[count][9] != null && aryTaxinv[count][9].length() > 0) {
            where += "AND T.ADJ_NO = " + aryTaxinv[count][9] + " ";
        }

        if (aryTaxinv[count][10] != null && aryTaxinv[count][10].length() > 0) {
            where += "AND T.XFR_NO = " + aryTaxinv[count][10] + " ";
        }

        if (aryTaxinv[count][11] != null && aryTaxinv[count][11].length() > 0) {
            where += "AND T.DOCUMENT_NO = '" + aryTaxinv[count][11] + "' ";
        }

        query = query + where + " ORDER BY T.STORE, T.INV_DATE DESC, T.PROCESS_ID ";

        return (query);
    }

    private String getPrintAllDetailQuery(String type) throws SQLException {
        String query = "";
        String select =
            "SELECT CD.SHORT_SKU, I.ITEM_DESC, CD.CM_MARGIN, SUM(CD.QTY_CM) AS QTY_CM, SUM(CD.EXT_COST_CM) AS EXT_COST_CM ";
        String from = "FROM TAXINV T, CMDET CD, ITEMMST I" + ", CMHDR CH "; //added by Mega 20150925 for AU-SD011432058
        String where =
            "WHERE T.STORE = CD.STORE " + "AND T.COY = CD.COY " + "AND T.COY_SUB = CD.COY_SUB " +
            "AND T.DOCUMENT_NO = CD.DOCUMENT_NO " + "AND T.DOC_TYPE = 'CM' ";
        //added by Mega 20150925 for AU-SD011432058 START
        where =
            where + "AND T.INVOICE_NO = CH.INVOICE_NO " + "AND CD.SHORT_SKU = I.SHORT_SKU " + "AND CD.COY = CH.COY " +
            "AND CD.COY_SUB = CH.COY_SUB " + "AND CD.STORE = CH.STORE " + "AND CD.DOCUMENT_NO = CH.DOCUMENT_NO " +
            "AND CD.CM_RSN_CD = CH.CM_RSN_CD " + "AND CD.CM_NO = CH.CM_NO ";
        if (type.equals("VALUE")) {
            where = where + "AND CH.INVOICE_NO = ? ";
        }
        //added by Mega 20150925 for AU-SD011432058 END

        String expression = "";
        String groupBy = "GROUP BY CD.SHORT_SKU, I.ITEM_DESC, CD.CM_MARGIN ";
        String orderBy = "ORDER BY CD.SHORT_SKU, I.ITEM_DESC, CD.CM_MARGIN ";

        if (taxinvSQL.COY() != null && taxinvSQL.COY().length() > 0) {
            expression += "AND T.COY = '" + taxinvSQL.COY() + "' ";
        }

        if (taxinvSQL.COY_SUB() != null && taxinvSQL.COY().length() > 0) {
            expression += "AND T.COY_SUB = '" + taxinvSQL.COY() + "' ";
        }

        if (taxinvSQL.STORE() != null && taxinvSQL.STORE().length() > 0) {
            expression += "AND T.STORE = " + "'" + taxinvSQL.STORE() + "' ";
        }

        if (taxinvSQL.PROCESS_ID() != null && taxinvSQL.PROCESS_ID().length() > 0) {
            expression += "AND T.PROCESS_ID = '" + taxinvSQL.PROCESS_ID() + "' ";
        }

        if (taxinvSQL.DEBIT_NOTE_NO() != null && taxinvSQL.DEBIT_NOTE_NO().length() > 0) {
            expression += "AND T.DEBIT_NOTE_NO = '" + taxinvSQL.DEBIT_NOTE_NO() + "' ";
        }

        if (String.valueOf(taxinvSQL.CRED_CLAIM_NO()).length() > 0) {
            expression += "AND T.CRED_CLAIM_NO = " + String.valueOf(taxinvSQL.CRED_CLAIM_NO()) + " ";
        }

        if (String.valueOf(taxinvSQL.ADJ_NO()).length() > 0) {
            expression += "AND T.ADJ_NO = " + String.valueOf(taxinvSQL.ADJ_NO()) + " ";
        }

        if (String.valueOf(taxinvSQL.XFR_NO()).length() > 0) {
            expression += "AND T.XFR_NO = " + String.valueOf(taxinvSQL.XFR_NO()) + " ";
        }

        if (taxinvSQL.DOCUMENT_NO() != null && taxinvSQL.DOCUMENT_NO().length() > 0) {
            expression += "AND T.DOCUMENT_NO = '" + taxinvSQL.DOCUMENT_NO() + "' ";
        }

        query = select + from + where + expression + groupBy + orderBy;

        if (type.equals("COUNT")) {
            query = "SELECT COUNT(*) AS TOTAL_NUM FROM (" + query + ") ";
        }

        return (query);
    }

    private String getDetailQuery(int count, String type) throws SQLException {
        String query = "";
        String select =
            "SELECT CD.SHORT_SKU, I.ITEM_DESC, CD.CM_MARGIN, SUM(CD.QTY_CM) AS QTY_CM, SUM(CD.EXT_COST_CM) AS EXT_COST_CM ";
        String from = "FROM TAXINV T, CMDET CD, ITEMMST I" + ", CMHDR CH "; //added by Mega 20150925 for AU-SD011432058
        String where =
            "WHERE T.STORE = CD.STORE " + "AND T.COY = CD.COY " + "AND T.COY_SUB = CD.COY_SUB " +
            "AND T.DOCUMENT_NO = CD.DOCUMENT_NO " + "AND T.DOC_TYPE = 'CM' ";
        //added by Mega 20150925 for AU-SD011432058 START
        where =
            where + "AND T.INVOICE_NO = CH.INVOICE_NO " + "AND CD.SHORT_SKU = I.SHORT_SKU " + "AND CD.COY = CH.COY " +
            "AND CD.COY_SUB = CH.COY_SUB " + "AND CD.STORE = CH.STORE " + "AND CD.DOCUMENT_NO = CH.DOCUMENT_NO " +
            "AND CD.CM_RSN_CD = CH.CM_RSN_CD " + "AND CD.CM_NO = CH.CM_NO ";
        if (type.equals("VALUE")) {
            where = where + "AND CH.INVOICE_NO = ? ";
        }
        //added by Mega 20150925 for AU-SD011432058 END

        String expression = "";
        String groupBy = "GROUP BY CD.SHORT_SKU, I.ITEM_DESC, CD.CM_MARGIN ";
        String orderBy = "ORDER BY CD.SHORT_SKU, I.ITEM_DESC, CD.CM_MARGIN ";

        if (aryTaxinv[count][0] != null && aryTaxinv[count][0].length() > 0) {
            expression += "AND T.COY = '" + aryTaxinv[count][0] + "' ";
        }

        if (aryTaxinv[count][1] != null && aryTaxinv[count][1].length() > 0) {
            expression += "AND T.COY_SUB = '" + aryTaxinv[count][1] + "' ";
        }

        if (aryTaxinv[count][2] != null && aryTaxinv[count][2].length() > 0) {
            expression += "AND T.STORE = " + "'" + aryTaxinv[count][2] + "' ";
        }

        if (aryTaxinv[count][3] != null && aryTaxinv[count][3].length() > 0) {
            expression += "AND T.PROCESS_ID = '" + aryTaxinv[count][3] + "' ";
        }

        if (aryTaxinv[count][7] != null && aryTaxinv[count][7].length() > 0) {
            expression += "AND T.DEBIT_NOTE_NO = '" + aryTaxinv[count][7] + "' ";
        }

        if (aryTaxinv[count][8] != null && aryTaxinv[count][8].length() > 0) {
            expression += "AND T.CRED_CLAIM_NO = " + aryTaxinv[count][8] + " ";
        }

        if (aryTaxinv[count][9] != null && aryTaxinv[count][9].length() > 0) {
            expression += "AND T.ADJ_NO = " + aryTaxinv[count][9] + " ";
        }

        if (aryTaxinv[count][10] != null && aryTaxinv[count][10].length() > 0) {
            expression += "AND T.XFR_NO = " + aryTaxinv[count][10] + " ";
        }

        if (aryTaxinv[count][11] != null && aryTaxinv[count][11].length() > 0) {
            expression += "AND T.DOCUMENT_NO = '" + aryTaxinv[count][11] + "' ";
        }

        query = select + from + where + expression + groupBy + orderBy;

        if (type.equals("COUNT")) {
            query = "SELECT COUNT(*) AS TOTAL_NUM FROM (" + query + ") ";
        }

        return (query);
    }

    private void printConcessTaxInvoice(int num) throws Exception {
        String strHdrQuery = "";
        String strDetQuery = "";
        
        strHdrQuery = getHeaderQuery(num);
        psHdr = conn.prepareStatement(strHdrQuery);
        rsHdr = psHdr.executeQuery();

        strDetQuery = getDetailQuery(num, "COUNT");
        psDet = conn.prepareStatement(strDetQuery);
        rsDet = psDet.executeQuery();


        Table datatable = null;
        Table blankTable = null;
        Table createTotalTable = null;

        int countValue = 1;
        int totalData = 0;

        if (rsDet != null && rsDet.next()) {
            totalData = rsDet.getInt("TOTAL_NUM");
        }

        intGlobalCount = 1;
        intPageNum = 1;
        intPageTotal = (int) Math.ceil((double) totalData / intMaxRecord);

        if (intPageTotal < 1) {
            intPageTotal = 1;
        }

        if (rsHdr != null && rsHdr.next()) {
            taxinvSQL.populate(rsHdr);

            getCOYMST(taxinvSQL.COY());
            getCOYSUBMST(taxinvSQL.COY(), taxinvSQL.COY_SUB());
            getSTRMST(taxinvSQL.STORE());

            getTaxRptFormat();
                
            strDetQuery = getDetailQuery(num, "VALUE");
            psDet = conn.prepareStatement(strDetQuery);
            psDet.setString(1, rsHdr.getString("INVOICE_NO"));
            rsDet = psDet.executeQuery();

            if (totalData > 0) {
                while (rsDet != null && rsDet.next()) {
                    if (intGlobalCount == 1 || countValue == 1) {
                        printNewPage();
                        initializeHeaderTable();
                        printHeader(tableSuperSuperHdr, tableSuperHdr, tableSuperTableHdr, tableHdr);
                    }

                    datatable = createDataTable(countValue);
                    document.add(datatable);

                    if (countValue == intMaxRecord || intGlobalCount == totalData) {
                        if (intGlobalCount == totalData && totalData % intMaxRecord != 0) {
                            for (int k = countValue + 1; k <= intMaxRecord; k++) {
                                blankTable = createBlankTable(k);
                                document.add(blankTable);
                            }

                            countValue = intMaxRecord;
                        }

                        if (intGlobalCount == totalData) {
                            blankTable = createTotalTable();
                            document.add(blankTable);
                        } else {
                            blankTable = createBlankTotalTable();
                            document.add(blankTable);
                        }

                        printTableFooter();

                        intPageNum++;
                    }

                    countValue++;
                    intGlobalCount++;

                    if (countValue > intMaxRecord) {
                        countValue = 1;
                    }
                }
            } else {
                printNewPage();
                initializeHeaderTable();
                printHeader(tableSuperSuperHdr, tableSuperHdr, tableSuperTableHdr, tableHdr);

                for (int k = 1; k <= intMaxRecord; k++) {
                    blankTable = createBlankTable(k);
                    document.add(blankTable);
                }

                blankTable = createEmptyTotalTable();
                document.add(blankTable);

                printTableFooter();
            }

            initTotal();
        } else {
            printNewPage();
            printIncreaseOneLine();
            printBlankPage();
            printBlankCell(1);
        }

        if (rsDet != null) {
            rsDet.close();
            rsDet = null;
        }

        if (rsHdr != null) {
            rsHdr.close();
            rsHdr = null;
        }

        if (psDet != null) {
            psDet.close();
            psDet = null;
        }

        if (psHdr != null) {
            psHdr.close();
            psHdr = null;
        }
    }

    private void printAllConcessTaxInvoice() throws Exception {
        String strHdrQuery = "";
        String strDetQuery = "";

        Table datatable = null;
        Table blankTable = null;
        Table createTotalTable = null;

        int countValue = 1;
        int totalData = 0;

        strHdrQuery = getPrintAllHeaderQuery();
        psHdr = conn.prepareStatement(strHdrQuery);
        rsHdr = psHdr.executeQuery();

        while (rsHdr != null && rsHdr.next()) {
            taxinvSQL.populate(rsHdr);

            getCOYMST(taxinvSQL.COY());
            getCOYSUBMST(taxinvSQL.COY(), taxinvSQL.COY_SUB());
            getSTRMST(taxinvSQL.STORE());

            getTaxRptFormat();
            
            strDetQuery = getPrintAllDetailQuery("COUNT");
            psDet = conn.prepareStatement(strDetQuery);
            rsDet = psDet.executeQuery();

            if (rsDet != null && rsDet.next()) {
                totalData = rsDet.getInt("TOTAL_NUM");
            }

            intGlobalCount = 1;
            intPageNum = 1;
            intPageTotal = (int) Math.ceil((double) totalData / intMaxRecord);

            if (intPageTotal < 1) {
                intPageTotal = 1;
            }

            if (rsDet != null) {
                rsDet.close();
                rsDet = null;
            }

            if (psDet != null) {
                psDet.close();
                psDet = null;
            }

            strDetQuery = getPrintAllDetailQuery("VALUE");
            psDet = conn.prepareStatement(strDetQuery);
            psDet.setString(1, rsHdr.getString("INVOICE_NO"));
            rsDet = psDet.executeQuery();

            if (totalData > 0) {
                while (rsDet != null && rsDet.next()) {
                    if (intGlobalCount == 1 || countValue == 1) {
                        printNewPage();
                        initializeHeaderTable();
                        printHeader(tableSuperSuperHdr, tableSuperHdr, tableSuperTableHdr, tableHdr);
                    }

                    datatable = createDataTable(countValue);
                    document.add(datatable);

                    if (countValue == intMaxRecord || intGlobalCount == totalData) {
                        if (intGlobalCount == totalData && totalData % intMaxRecord != 0) {
                            for (int k = countValue + 1; k <= intMaxRecord; k++) {
                                blankTable = createBlankTable(k);
                                document.add(blankTable);
                            }

                            countValue = intMaxRecord;
                        }

                        if (intGlobalCount == totalData) {
                            blankTable = createTotalTable();
                            document.add(blankTable);
                        } else {
                            blankTable = createBlankTotalTable();
                            document.add(blankTable);
                        }

                        printTableFooter();

                        intPageNum++;
                    }

                    countValue++;
                    intGlobalCount++;

                    if (countValue > intMaxRecord) {
                        countValue = 1;
                    }
                }
            } else {
                printNewPage();
                initializeHeaderTable();
                printHeader(tableSuperSuperHdr, tableSuperHdr, tableSuperTableHdr, tableHdr);

                for (int k = 1; k <= intMaxRecord; k++) {
                    blankTable = createBlankTable(k);
                    document.add(blankTable);
                }

                blankTable = createEmptyTotalTable();
                document.add(blankTable);

                printTableFooter();
            }

            initTotal();

            if (rsDet != null) {
                rsDet.close();
                rsDet = null;
            }

            if (psDet != null) {
                psDet.close();
                psDet = null;
            }
        }

        if (rsDet != null) {
            rsDet.close();
            rsDet = null;
        }

        if (rsHdr != null) {
            rsHdr.close();
            rsHdr = null;
        }

        if (psDet != null) {
            psDet.close();
            psDet = null;
        }

        if (psHdr != null) {
            psHdr.close();
            psHdr = null;
        }
    }

    private void getTaxRptFormat() throws SQLException {
        StatementManager manager = null;
        String query;
        Parameters parameters;
        String strDateCtrl;
        Date updatedDate = null;
        taxrptctrlSQL.setDOC_TYPE(DOC_TYPE);

        if (taxrptctrlSQL.getDateCtrlByDocType() > 0) {
            strDateCtrl = taxrptctrlSQL.DATE_CTRL();
            if (strDateCtrl != null) {
                try {
                    manager = StatementManager.newInstance(conn);
                    query = "SELECT " + strDateCtrl + " FROM TAXINV WHERE PROCESS_ID = ? AND DOCUMENT_NO= ?";
                    parameters = Parameters.builder().add(taxinvSQL.PROCESS_ID()).add(taxinvSQL.DOCUMENT_NO());
                    ResultSet rs = manager.select(query, parameters);

                    if (rs != null && rs.next()) {
                        updatedDate = rs.getDate(strDateCtrl);
                    }

                    if (updatedDate != null) {
                        if (taxrptctrlSQL.getByDateRange(updatedDate) > 0) {
                            strReportFormat = taxrptctrlSQL.RPT_FORMAT();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    if (manager != null) {
                        manager.close(false);
                        manager = null;
                    }
                }
            }
        }
    }
    
    private void printNewPage() throws Exception {
        document.newPage();
        dbltotal = 0;
    }

    private void initializeHeaderTable() throws Exception {
        tableSuperSuperHdr = createTableSuperSuperHdr();
        tableSuperHdr = createTableSuperHdr();
        tableSuperTableHdr = createSuperTableHdr();
        tableHdr = createTableHdr();
    }

    private void printHeader(Table hdr1, Table hdr2, Table hdr3, Table hdr4) throws Exception {
        document.add(hdr1);
        document.add(hdr2);
        document.add(hdr3);

        printBlankCell(1);
        document.add(hdr4);
    }

    private Table createTableSuperSuperHdr() throws BadElementException, DocumentException, SQLException, Exception {
        Table tableSuperSuperHdr = new Table(3);

        int headerwidths[] = { 20, 3, 77 };
        tableSuperSuperHdr.setPadding(0.5f);
        tableSuperSuperHdr.setSpacing(0);
        tableSuperSuperHdr.setWidths(headerwidths);
        tableSuperSuperHdr.setWidth(100);
        tableSuperSuperHdr.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
        tableSuperSuperHdr.setBorder(Rectangle.NO_BORDER);

        String strCompName = "";
        String strAddress1 = "";
        String strAddress2 = "";
        String strTelAndFax = "";
        String strGSTRegNo = "\n";

        Cell cell = null;

        //    strCompName  = getDescription(coymstSQL.COY_NAME()) + ". (126926-H)";
        strCompName = SYSCompanyName + " " + SYSCompanyRegNo; //fadhilah 3/feb/16
        strAddress1 =
            "Head Office: " + getDescription(coymstSQL.COY_ADDR1()) + " " + getDescription(coymstSQL.COY_ADDR2());
        strAddress2 = getDescription(coymstSQL.COY_ADDR3());
        strTelAndFax = "Tel: " + coymstSQL.COY_PHONE() + " Fax: " + coymstSQL.COY_FAX();
        
        if(strReportFormat.equals("1")) {
            strGSTRegNo = adlangmstSQL.getTranslatedCaptionMsg(user_lang, "GST") + " Reg. No.: " +
                coymstSQL.COY_TAX_REG_NO();
        }

        Image company_logo_image = null;
        Image the_line_image = null;

        try {
            company_logo_image = Image.getInstance(SYSCompanyLogo);
            company_logo_image.scalePercent(100);

            the_line_image = Image.getInstance(SYSReportLine);
            the_line_image.scaleAbsolute(410, 2);
        } catch (Exception e) {
        }

        if (company_logo_image != null) {
            cell = new Cell(company_logo_image);
        } else {
            cell = new Cell(new Phrase(""));
        }

        cell.setRowspan(6);
        cell.disableBorderSide(Rectangle.BOX);
        cell.setVerticalAlignment(PdfPCell.ALIGN_CENTER);
        tableSuperSuperHdr.addCell(cell);

        cell = new Cell(new Phrase(""));
        cell.disableBorderSide(Rectangle.BOX);
        tableSuperSuperHdr.addCell(cell);

        cell = new Cell(new Phrase(strCompName, FontHeaderType)); // Company Name
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperSuperHdr.addCell(cell);

        if (the_line_image != null) {
            cell = new Cell(the_line_image);
        } else {
            cell = new Cell(new Phrase(""));
        }

        cell.setColspan(2);
        cell.disableBorderSide(Rectangle.BOX);
        tableSuperSuperHdr.addCell(cell);

        cell = new Cell(new Phrase(""));
        cell.disableBorderSide(Rectangle.BOX);
        tableSuperSuperHdr.addCell(cell);

        cell = new Cell(new Phrase(strAddress1, FontHeaderType)); // Company Address 1
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperSuperHdr.addCell(cell);

        cell = new Cell(new Phrase(""));
        cell.setRowspan(4);
        cell.disableBorderSide(Rectangle.BOX);
        tableSuperSuperHdr.addCell(cell);

        cell = new Cell(new Phrase(strAddress2, FontHeaderType)); // Company Address 2
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperSuperHdr.addCell(cell);

        cell = new Cell(new Phrase(strTelAndFax, FontHeaderType)); // Company Tel And Fax
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperSuperHdr.addCell(cell);

        //fadhilah 3/feb/16
        if ((SYSRptIdn != "" || SYSRptIdn != null) && SYSRptIdn.equals("1")) {
        } else {
            cell = new Cell(new Phrase(strGSTRegNo, FontHeaderType)); // Company GST Registration Number
            cell.disableBorderSide(Rectangle.BOX);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            tableSuperSuperHdr.addCell(cell);
        }
        return tableSuperSuperHdr;
    }

    private Table createTableSuperHdr() throws BadElementException, DocumentException, SQLException {
        Table tableSuperHdr = new Table(3);

        int headerwidths[] = { 30, 40, 30 };

        tableSuperHdr.setPadding(0);
        tableSuperHdr.setSpacing(0);
        tableSuperHdr.setWidths(headerwidths);
        tableSuperHdr.setWidth(100);
        tableSuperHdr.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
        tableSuperHdr.setBorder(Rectangle.NO_BORDER);

        Cell cell = null;

        cell = new Cell(new Phrase("", FontTitleType));
        cell.disableBorderSide(Rectangle.BOX);
        tableSuperHdr.addCell(cell);

        String titleTaxInvoice;
        if(strReportFormat.equals("1")) {
            titleTaxInvoice = "TAX INVOICE";
        } else {
            titleTaxInvoice = "INVOICE";
        }
        cell = new Cell(new Phrase(titleTaxInvoice, FontTitleType));
        cell.disableBorderSide(Rectangle.BOX);
        tableSuperHdr.addCell(cell);

        cell = new Cell(new Phrase("", FontTitleType));
        cell.disableBorderSide(Rectangle.BOX);
        tableSuperHdr.addCell(cell);

        return tableSuperHdr;
    }

    private Table createSuperTableHdr() throws BadElementException, DocumentException, SQLException, Exception {
        Table tableSuperTableHdr = new Table(7);

        int headerwidths[] = { 17, 2, 45, 2, 10, 2, 22 };

        tableSuperTableHdr.setPadding(0.5f);
        tableSuperTableHdr.setSpacing(0);
        tableSuperTableHdr.setWidths(headerwidths);
        tableSuperTableHdr.setWidth(100);
        tableSuperTableHdr.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
        tableSuperTableHdr.setBorder(Rectangle.NO_BORDER);

        String strCustName = "";
        String strCustName1 = "";
        String strCustName2 = "";
        String strInvoiceNo = "";
        String strSupplCode = "";
        String strInvDate = "";
        String strAddress = "";
        String strAddress1 = "";
        String strAddress2 = "";
        String strAddress3 = "";
        String strAddress4 = "";
        String strDocumentID = "";
        String strStoreCode = "";
        String strStoreName = "";
        String strStoreName1 = "";
        String strStoreName2 = "";
        String strGSTRegNo = "";
        String strGSTRate = "";

        strCustName = taxinvSQL.CUSTOMER_NAME().trim();
        strSupplCode = taxinvSQL.SUPPL();
        strAddress = taxinvSQL.ADDRESS().trim();
        strInvDate = qrMisc.parseDate(taxinvSQL.DATE_CONFIRM(), strDateFormat);
        strDocumentID = taxinvSQL.DOCUMENT_NO();
        strStoreCode = taxinvSQL.STORE();
        strStoreName = getDescription(strmstSQL.STORE_NAME()).trim();
        strGSTRegNo = taxinvSQL.TAX_CODE();
        strGSTRate = currencyConverter.format(taxinvSQL.INV_VAT_RATE()) + "%";

        if (strCustName.length() > intHdrDescSize) {
            wordWraping(strCustName, intHdrDescSize);

            strCustName1 = aryWordWrap[0];
            strCustName2 = aryWordWrap[1];
        } else {
            strCustName1 = strCustName;
            strCustName2 = "";
        }

        if (strStoreName.length() > intSYSInvStrNameLength) {
            wordWraping(strStoreName, intSYSInvStrNameLength);

            strStoreName1 = aryWordWrap[0];
            strStoreName2 = aryWordWrap[1];
        } else {
            strStoreName1 = strStoreName;
            strStoreName2 = "";
        }

        for (int i = 1; i < formula.length; i++) {
            if (formula[i].equals("F")) {
                strInvoiceNo += taxinvSQL.FORM();
            } else if (formula[i].equals("S")) {
                strInvoiceNo += taxinvSQL.SERIAL_NO();
            } else if (formula[i].equals("I")) {
                strInvoiceNo += taxinvSQL.INVOICE_NO();
            } else {
                strInvoiceNo += formula[i];
            }
        }

        strInvoiceNo = rsHdr.getString("INV_NO");

        //fadhilah 3/feb/16
        if (strAddress.trim().length() > 0) {
            if ((SYSRptIdn != "" || SYSRptIdn != null) && SYSRptIdn.equals("1") &&
                (strAddress.charAt(strAddress.length() - 3) == 'I')) {
                strAddress = strAddress.substring(0, strAddress.length() - 3);
            } else {
                strAddress = strAddress;
            }
        }

        if (strAddress.length() > intHdrDescSize) {
            wordWraping(strAddress, intHdrDescSize);

            strAddress1 = aryWordWrap[0];
            strAddress2 = aryWordWrap[1];

            if (aryWordWrap.length <= 2) {
                strAddress3 = "";
            } else {
                strAddress3 = aryWordWrap[2];
            }

            if (aryWordWrap.length <= 3) {
                strAddress4 = "";
            } else {
                strAddress4 = aryWordWrap[3];
            }
        } else {
            strAddress1 = strAddress;
            strAddress2 = "";
            strAddress3 = "";
            strAddress4 = "";
        }

        Cell cell = null;

        cell = new Cell(new Phrase("To", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(" : ", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strCustName1, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("Invoice No.", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(" : ", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strInvoiceNo, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        //-----------------------------------------------------------------------------------------------//

        if (strCustName2.length() > 0) {
            addHdrReportLine1 = true;

            cell = new Cell(new Phrase("", FontDetailType));
            cell.disableBorderSide(Rectangle.BOX);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            tableSuperTableHdr.addCell(cell);

            cell = new Cell(new Phrase("", FontDetailType));
            cell.disableBorderSide(Rectangle.BOX);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            tableSuperTableHdr.addCell(cell);

            cell = new Cell(new Phrase(strCustName2, FontDetailType));
            cell.disableBorderSide(Rectangle.BOX);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            tableSuperTableHdr.addCell(cell);

            cell = new Cell(new Phrase("", FontDetailType));
            cell.disableBorderSide(Rectangle.BOX);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
            tableSuperTableHdr.addCell(cell);

            cell = new Cell(new Phrase("", FontDetailType));
            cell.disableBorderSide(Rectangle.BOX);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            tableSuperTableHdr.addCell(cell);

            cell = new Cell(new Phrase("", FontDetailType));
            cell.disableBorderSide(Rectangle.BOX);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            tableSuperTableHdr.addCell(cell);

            cell = new Cell(new Phrase("", FontDetailType));
            cell.disableBorderSide(Rectangle.BOX);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            tableSuperTableHdr.addCell(cell);
        }

        //-----------------------------------------------------------------------------------------------//

        cell = new Cell(new Phrase("Supplier Code", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(" : ", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strSupplCode, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("Date", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(" : ", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strInvDate, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        //-----------------------------------------------------------------------------------------------//

        cell = new Cell(new Phrase("Address", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(" : ", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strAddress1, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("Document ID", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(" : ", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strDocumentID, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        //-----------------------------------------------------------------------------------------------//

        cell = new Cell(new Phrase("", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strAddress2, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("Store Code", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(" : ", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strStoreCode, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        //-----------------------------------------------------------------------------------------------//

        cell = new Cell(new Phrase("", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strAddress3, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("Store Name", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(" : ", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strStoreName1, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        //-----------------------------------------------------------------------------------------------//

        if (strStoreName2.length() > 0 || strAddress4.length() > 0) {
            addHdrReportLine2 = true;

            cell = new Cell(new Phrase("", FontDetailType));
            cell.disableBorderSide(Rectangle.BOX);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            tableSuperTableHdr.addCell(cell);

            cell = new Cell(new Phrase("", FontDetailType));
            cell.disableBorderSide(Rectangle.BOX);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            tableSuperTableHdr.addCell(cell);

            cell = new Cell(new Phrase(strAddress4, FontDetailType));
            cell.disableBorderSide(Rectangle.BOX);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            tableSuperTableHdr.addCell(cell);

            cell = new Cell(new Phrase("", FontDetailType));
            cell.disableBorderSide(Rectangle.BOX);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
            tableSuperTableHdr.addCell(cell);

            cell = new Cell(new Phrase("", FontDetailType));
            cell.disableBorderSide(Rectangle.BOX);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            tableSuperTableHdr.addCell(cell);

            cell = new Cell(new Phrase("", FontDetailType));
            cell.disableBorderSide(Rectangle.BOX);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            tableSuperTableHdr.addCell(cell);

            cell = new Cell(new Phrase(strStoreName2, FontDetailType));
            cell.disableBorderSide(Rectangle.BOX);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            tableSuperTableHdr.addCell(cell);
        }

        //-----------------------------------------------------------------------------------------------//
        String suppGstRegNoLabel;
        String gstRateLabel;
        String strColonText;
        if(strReportFormat.equals("1")) {
            suppGstRegNoLabel = "Supplier " + adlangmstSQL.getTranslatedCaptionMsg(user_lang, "GST") + " Reg. No.";
            gstRateLabel = adlangmstSQL.getTranslatedCaptionMsg(user_lang, "GST") + " Rate";
            strColonText = " : ";
        } else {
            suppGstRegNoLabel = "\n";
            strGSTRegNo = "\n";
            gstRateLabel = "\n";
            strGSTRate = "\n";
            strColonText = "\n";
        }

        cell = new Cell(new Phrase(suppGstRegNoLabel, FontDetailType)); //fadhilah 3/2/16
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strColonText, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strGSTRegNo, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(gstRateLabel, FontDetailType)); //fadhilah 3/2/16
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strColonText, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strGSTRate, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        //-----------------------------------------------------------------------------------------------//

        cell = new Cell(new Phrase("", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        tableSuperTableHdr.addCell(cell);
        cell.setColspan(7);

        return tableSuperTableHdr;
    }

    private Table createTableHdr() throws BadElementException, DocumentException, SQLException, Exception {
        Table tableHeader = new Table(6);

        int headerwidths[] = { 5, 12, 45, 12, 10, 16 };

        tableHeader.setPadding(2);
        tableHeader.setSpacing(0);
        tableHeader.setWidths(headerwidths);
        tableHeader.setWidth(100);
        tableHeader.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
        tableHeader.setBorder(Rectangle.NO_BORDER);

        Cell cell = null;

        cell = new Cell(new Phrase("NO.", FontChinese));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setGrayFill(IReport.STD_GRAY);
        tableHeader.addCell(cell);

        cell = new Cell(new Phrase("ITEM CODE", FontTableType));
        cell.setGrayFill(IReport.STD_GRAY);
        tableHeader.addCell(cell);

        cell = new Cell(new Phrase("ITEM DESCRIPTION", FontTableType));
        cell.setGrayFill(IReport.STD_GRAY);
        tableHeader.addCell(cell);

        cell = new Cell(new Phrase("MARGIN (%)", FontTableType));
        cell.setGrayFill(IReport.STD_GRAY);
        tableHeader.addCell(cell);

        cell = new Cell(new Phrase("QTY", FontTableType));
        cell.setGrayFill(IReport.STD_GRAY);
        tableHeader.addCell(cell);

        cell = new Cell(new Phrase("AMOUNT (" + SYSCurrCom + ")", FontTableType));
        cell.setGrayFill(IReport.STD_GRAY);
        tableHeader.addCell(cell);

        return tableHeader;
    }

    private Table createDataTable(int count) throws BadElementException, DocumentException, SQLException {
        Table datatable = new Table(6);

        int headerwidths[] = { 5, 12, 45, 12, 10, 16 };

        datatable.setPadding(0);
        datatable.setSpacing(0);
        datatable.setWidths(headerwidths);
        datatable.setWidth(100);
        datatable.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
        datatable.setDefaultVerticalAlignment(Element.ALIGN_TOP);
        datatable.setBorder(Rectangle.NO_BORDER);

        String strNumber = "";
        String strItemCode = "";
        String strItemDesc = "";
        String strItemDescRow1 = "";
        String strItemDescRow2 = "";
        String strMargin = "";
        String strQty = "";
        String strAmount = "";

        strNumber = String.valueOf(count);
        strItemCode = rsDet.getString("SHORT_SKU");
        strItemDesc = getDescription(rsDet.getString("ITEM_DESC")).trim();
        strMargin = currencyConverter.format(rsDet.getDouble("CM_MARGIN"));
        strQty = currencyConverter.sformat(rsDet.getDouble("QTY_CM"), 0);
        strAmount = currencyConverter.format(rsDet.getDouble("EXT_COST_CM"));

        if (strItemDesc.length() > intDataDescSize) {
            wordWraping(strItemDesc, intDataDescSize);

            strItemDescRow1 = aryWordWrap[0];
            strItemDescRow2 = aryWordWrap[1];
        } else {
            strItemDescRow1 = strItemDesc;
            strItemDescRow2 = "\n";
        }

        Cell cell = null;

        cell = new Cell(new Phrase(strNumber, FontDataType));
        cell.setNoWrap(true);
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        datatable.addCell(cell);

        cell = new Cell(new Phrase(strItemCode, FontDataType));
        cell.setNoWrap(true);
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        datatable.addCell(cell);

        cell = new Cell(new Phrase(strItemDescRow1, FontDataType));
        cell.setNoWrap(true);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.disableBorderSide(Rectangle.BOX);
        cell.enableBorderSide(Rectangle.TOP);
        cell.enableBorderSide(Rectangle.LEFT);
        cell.enableBorderSide(Rectangle.RIGHT);
        datatable.addCell(cell);

        cell = new Cell(new Phrase(strMargin, FontDataType));
        cell.setNoWrap(true);
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        datatable.addCell(cell);

        cell = new Cell(new Phrase(strQty, FontDataType));
        cell.setNoWrap(true);
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        datatable.addCell(cell);

        cell = new Cell(new Phrase(strAmount, FontDataType));
        cell.setNoWrap(true);
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        datatable.addCell(cell);

        cell = new Cell(new Phrase(strItemDescRow2, FontDataType));
        cell.setNoWrap(true);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.disableBorderSide(Rectangle.BOX);
        cell.enableBorderSide(Rectangle.LEFT);
        cell.enableBorderSide(Rectangle.RIGHT);
        cell.enableBorderSide(Rectangle.BOTTOM);
        datatable.addCell(cell);

        return datatable;
    }

    private Table createBlankTable(int count) throws Exception {
        Table datatable = new Table(6);

        int headerwidths[] = { 5, 12, 45, 12, 10, 16 };

        datatable.setPadding(0);
        datatable.setSpacing(0);
        datatable.setWidths(headerwidths);
        datatable.setWidth(100);
        datatable.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
        datatable.setDefaultVerticalAlignment(Element.ALIGN_MIDDLE);
        datatable.setBorder(Rectangle.NO_BORDER);

        String strNumber = String.valueOf(count);
        String strItemCode = "";
        String strItemDesc = "\n";
        String strMargin = "";
        String strQty = "";
        String strAmount = "";

        Cell cell = null;

        cell = new Cell(new Phrase(strNumber, FontDataType));
        cell.setNoWrap(true);
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        datatable.addCell(cell);

        cell = new Cell(new Phrase(strItemCode, FontDataType));
        cell.setNoWrap(true);
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        datatable.addCell(cell);

        cell = new Cell(new Phrase(strItemDesc, FontDataType));
        cell.setNoWrap(true);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.disableBorderSide(Rectangle.BOX);
        cell.enableBorderSide(Rectangle.TOP);
        cell.enableBorderSide(Rectangle.LEFT);
        cell.enableBorderSide(Rectangle.RIGHT);
        datatable.addCell(cell);

        cell = new Cell(new Phrase(strMargin, FontDataType));
        cell.setNoWrap(true);
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        datatable.addCell(cell);

        cell = new Cell(new Phrase(strQty, FontDataType));
        cell.setNoWrap(true);
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        datatable.addCell(cell);

        cell = new Cell(new Phrase(strAmount, FontDataType));
        cell.setNoWrap(true);
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        datatable.addCell(cell);

        cell = new Cell(new Phrase(strItemDesc, FontDataType));
        cell.setNoWrap(true);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.disableBorderSide(Rectangle.BOX);
        cell.enableBorderSide(Rectangle.LEFT);
        cell.enableBorderSide(Rectangle.RIGHT);
        cell.enableBorderSide(Rectangle.BOTTOM);
        datatable.addCell(cell);

        return datatable;
    }

    private Table createTotalTable() throws Exception {
        Table tableTotal_ = new Table(6);

        int headerwidths[] = { 5, 12, 45, 12, 10, 16 };

        tableTotal_.setPadding(1);
        tableTotal_.setSpacing(0);
        tableTotal_.setWidths(headerwidths);
        tableTotal_.setWidth(100);
        tableTotal_.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
        tableTotal_.setBorder(Rectangle.NO_BORDER);

        String strTotalGST;
        String strTotalInGST;

        Cell cell = null;

        String totalLabel;
        String gstAmountLabel;
        String totalInclGstLabel;
        if(strReportFormat.equals("1")) {
            strTotalGST = currencyConverter.format(taxinvSQL.INV_VAT_AMOUNT());
            strTotalInGST = currencyConverter.format(taxinvSQL.INV_AMT_WVAT());
            totalLabel = "TOTAL EXCL. GST (" + SYSCurrCom + ")";
            gstAmountLabel = "GST AMOUNT (" + SYSCurrCom + ")";
            totalInclGstLabel = "TOTAL INCL. GST (" + SYSCurrCom + ")";
        } else {
            strTotalGST = "\n";
            strTotalInGST = "\n";
            totalLabel = "TOTAL (" + SYSCurrCom + ")";
            gstAmountLabel = "\n";
            totalInclGstLabel = "\n";
        }

        cell = new Cell(new Phrase("", FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setRowspan(3);
        cell.setColspan(3);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal_.addCell(cell);

        cell = new Cell(new Phrase(totalLabel, FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setColspan(2);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal_.addCell(cell);

        cell = new Cell(new Phrase(currencyConverter.format(taxinvSQL.INV_AMT_WOVAT()), FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        tableTotal_.addCell(cell);

        cell = new Cell(new Phrase(gstAmountLabel, FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setColspan(2);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal_.addCell(cell);

        cell = new Cell(new Phrase(strTotalGST, FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        tableTotal_.addCell(cell);

        cell = new Cell(new Phrase(totalInclGstLabel, FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setColspan(2);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal_.addCell(cell);

        cell = new Cell(new Phrase(strTotalInGST, FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        tableTotal_.addCell(cell);

        return tableTotal_;
    }

    private Table createEmptyTotalTable() throws Exception {
        Table tableTotal_ = new Table(6);

        int headerwidths[] = { 5, 12, 45, 12, 10, 16 };

        tableTotal_.setPadding(1);
        tableTotal_.setSpacing(0);
        tableTotal_.setWidths(headerwidths);
        tableTotal_.setWidth(100);
        tableTotal_.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
        tableTotal_.setBorder(Rectangle.NO_BORDER);

        String strTotalExGST = "";
        String strTotalGST = "";
        String strTotalInGST = "";

        Cell cell = null;

        strTotalExGST = "\n";
        strTotalGST = "\n";
        strTotalInGST = "\n";

        cell = new Cell(new Phrase("", FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setRowspan(3);
        cell.setColspan(3);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal_.addCell(cell);

        cell = new Cell(new Phrase("TOTAL EXCL. GST (" + SYSCurrCom + ")", FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setColspan(2);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal_.addCell(cell);

        cell = new Cell(new Phrase(strTotalExGST, FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        tableTotal_.addCell(cell);

        cell = new Cell(new Phrase("GST AMOUNT (" + SYSCurrCom + ")", FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setColspan(2);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal_.addCell(cell);

        cell = new Cell(new Phrase(strTotalGST, FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        tableTotal_.addCell(cell);

        cell = new Cell(new Phrase("TOTAL INCL. GST (" + SYSCurrCom + ")", FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setColspan(2);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal_.addCell(cell);

        cell = new Cell(new Phrase(strTotalInGST, FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        tableTotal_.addCell(cell);

        return tableTotal_;
    }

    private Table createBlankTotalTable() throws Exception {
        Table tableTotal_ = new Table(6);

        int headerwidths[] = { 5, 12, 45, 12, 10, 16 };

        tableTotal_.setPadding(1);
        tableTotal_.setSpacing(0);
        tableTotal_.setWidths(headerwidths);
        tableTotal_.setWidth(100);
        tableTotal_.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
        tableTotal_.setBorder(Rectangle.NO_BORDER);

        String strTotalExGST = "";
        String strTotalGST = "";
        String strTotalInGST = "";

        Cell cell = null;

        strTotalExGST = "\n";
        strTotalGST = "\n";
        strTotalInGST = "\n";

        cell = new Cell(new Phrase("", FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setRowspan(3);
        cell.setColspan(3);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal_.addCell(cell);

        cell = new Cell(new Phrase("\n", FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setColspan(2);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal_.addCell(cell);

        cell = new Cell(new Phrase(strTotalExGST, FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal_.addCell(cell);

        cell = new Cell(new Phrase("\n", FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setColspan(2);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal_.addCell(cell);

        cell = new Cell(new Phrase(strTotalGST, FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal_.addCell(cell);

        cell = new Cell(new Phrase("\n", FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setColspan(2);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal_.addCell(cell);

        cell = new Cell(new Phrase(strTotalInGST, FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal_.addCell(cell);

        return tableTotal_;
    }

    private void printTableFooter() throws BadElementException, DocumentException, SQLException {
        Table totalTable = new Table(1);

        int headerwidths[] = { 100 };

        totalTable.setPadding(0.5f);
        totalTable.setSpacing(0);
        totalTable.setWidths(headerwidths);
        totalTable.setWidth(100);
        totalTable.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
        totalTable.setBorder(Rectangle.NO_BORDER);

        String strPages = "Page " + String.valueOf(intPageNum) + "/" + String.valueOf(intPageTotal);

        int intLineCount = 2;

        Cell cell = null;

        cell = new Cell(new Phrase(strFooterMsg1, FontFooterType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.disableBorderSide(Rectangle.BOX);
        totalTable.addCell(cell);

        cell = new Cell(new Phrase(strFooterMsg2, FontFooterType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.disableBorderSide(Rectangle.BOX);
        totalTable.addCell(cell);

        cell = new Cell(new Phrase(strFooterMsg3, FontFooterType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.disableBorderSide(Rectangle.BOX);
        totalTable.addCell(cell);

        if (addHdrReportLine1) {
            intLineCount = intLineCount - 1;
            addHdrReportLine1 = false;
        }

        if (addHdrReportLine2) {
            intLineCount = intLineCount - 1;
            addHdrReportLine2 = false;
        }

        for (int a = 1; a <= intLineCount; a++) {
            cell = new Cell(new Phrase("\n", FontPagesType));
            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
            cell.disableBorderSide(Rectangle.BOX);
            totalTable.addCell(cell);
        }

        cell = new Cell(new Phrase(strPages, FontPagesType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.disableBorderSide(Rectangle.BOX);
        totalTable.addCell(cell);

        document.add(totalTable);
    }

    private void printBlankPage() throws Exception {
        Table table = new Table(1);
        table.disableBorderSide(Rectangle.BOX);

        if (printBlankPage == false) {
            Cell cell =
                new Cell(new Phrase(adlangmstSQL.getTranslatedCaptionMsg("1", "No Data To Print"), FontChinese));
            cell.disableBorderSide(Rectangle.BOX);
            table.addCell(cell);
            printBlankPage = true;
        } else {
            Cell cell = new Cell(new Phrase("\n", FontChinese));
            cell.disableBorderSide(Rectangle.BOX);
            table.addCell(cell);
        }

        document.add(table);
    }

    private void printBlankCell(int max) throws Exception {
        Table tableBlank = new Table(1);

        tableBlank.setPadding(2);
        tableBlank.setSpacing(0);
        tableBlank.setWidth(100);
        tableBlank.setBorder(Rectangle.NO_BORDER);

        Cell cell = null;

        cell = new Cell(new Phrase("", FontChinese));
        cell.disableBorderSide(Rectangle.BOX);

        for (int i = 0; i < max; i++) {
            tableBlank.addCell(cell);
        }
        document.add(tableBlank);
    }

    private void printIncreaseOneLine() throws Exception {
        Table tableIncreaseOneLine = new Table(1);

        tableIncreaseOneLine.setPadding((595f * 0.07f / 21.01f));
        tableIncreaseOneLine.setSpacing(0);
        tableIncreaseOneLine.setWidth(100);
        tableIncreaseOneLine.setBorder(Rectangle.NO_BORDER);

        Cell cell = null;

        cell = new Cell(new Phrase("", FontChinese));
        cell.disableBorderSide(Rectangle.BOX);
        tableIncreaseOneLine.addCell(cell);

        document.add(tableIncreaseOneLine);
    }

    private void printAfterSixRecordsBlankCell() throws Exception {
        Table tableBlank = new Table(1);

        tableBlank.setPadding(1);
        tableBlank.setSpacing(0);
        tableBlank.setWidth(100);
        tableBlank.setBorder(Rectangle.NO_BORDER);

        Cell cell = null;
        cell = new Cell(new Phrase("", FontChinese));
        cell.disableBorderSide(Rectangle.BOX);

        tableBlank.addCell(cell);
        document.add(tableBlank);
    }

    public void setTestRun(boolean bln) {
        test_run = bln;
    }

    protected void openConnection() throws SQLException {
        if (test_run) {
            // for test run using Jdevloper
            java.sql.DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
            conn =
                java.sql.DriverManager.getConnection("jdbc:oracle:thin:@188.188.8.9:1521:aeonmal", "profit",
                                                     "profit1888");
        } else {
            super.openConnection();
        }
    }

    private String retrieveAduser(String user) throws SQLException {
        aduserSQL.setUSR_ID(user);
        aduserSQL.getByKey();

        return aduserSQL.USR_FIRST_NAME();
    }

    private String getPROFITVV(String strVnm) throws Exception {
        profitvvSQL.setCOY(strCOY);
        profitvvSQL.setVNM(strVnm);
        profitvvSQL.getByKey();

        return profitvvSQL.VNM_VDTVL();
    }

    private void getCOYMST(String strCoy) throws Exception {
        coymstSQL.setCOY(strCoy);
        coymstSQL.getByKey();
    }

    private void getCOYSUBMST(String strCoy, String strCoySub) throws Exception {
        coysubmstSQL.setCOY(strCoy);
        coysubmstSQL.setCOY_SUB(strCoySub);
        coysubmstSQL.getByKey();
    }

    private void getSTRMST(String strStore) throws Exception {
        strmstSQL.setSTORE(strStore);
        strmstSQL.getByKey();
    }

    public String convertAmtInWords(double totalAmount, String coy, String userLang) throws Exception {
        String strTtlAmount = "";

        //    strTtlAmount = formatter.convertAmtInWords(totalAmount, adlangmstSQL.getTranslatedCaptionMsg(SYSLangCode, coysubmstSQL.FRGN_CRNCY_CD()), adlangmstSQL.getTranslatedCaptionMsg(SYSLangCode, "CENTS"));

        return strTtlAmount;
    }

    public void wordWraping(String strDesc, int intDescLength) {
        intTotalWrap = 1;
        strDescription = "";

        strDescription = wrap(strDesc, intDescLength);
        aryWordWrap = strDescription.split("\n");
    }

    public String wrap(String strDesc, int intDescLength) {
        int intLength = intDescLength;

        if (strDesc.length() < intLength) {
            return strDesc;
        }

        intTotalWrap++;

        int place =
            Math.max(Math.max(strDesc.lastIndexOf(" ", intLength), strDesc.lastIndexOf(",", intLength)),
                     strDesc.lastIndexOf("-", intLength));

        if (place < 0) {
            place = intLength;
        } else {
            place = place + 1;
        }

        return strDesc.substring(0, place) + "\n" + wrap(strDesc.substring(place), intDescLength);
    }

    private void closePreparedStatement(PreparedStatement ps) {
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (Exception e) {
        }
    }

    private void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
        }
    }

    public static void main(String[] args) {
        /*
    try
    {
      HParam hParam = new HParam();
      hParam.put("COY_SUB","AMY");
      hParam.put("USER_ID","DWIRA123");
      // hParam.put("STORE","1002");
      hParam.put("SUPPLIER","0000000183");
      // hParam.put("CONTRACT","OS-002");
      hParam.put("TRANS_DATE","2009-12-31");
      // hParam.put("BATCH_ID","13");
      // hParam.put("DEBIT_NOTE_NO","10010000001100");
      DebitNoteRpt printPrcAltAdj = new DebitNoteRpt("C:\\Temp\\DebitNoteRpt_2.pdf");
      printPrcAltAdj.setTestRun(true);
      printPrcAltAdj.print(hParam);
    }
    catch (Exception de)
    {
      de.printStackTrace();
    }
    System.exit(0);
    */
    }
}
