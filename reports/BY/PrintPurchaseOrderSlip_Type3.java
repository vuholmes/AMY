/**
 * Copyright (C) 2010 QR Retail Autotamation Sdn. Bhd.
 * All right reserved.
 *
 * Aeon Mal Project - Buying Module
 * Created by: Profit V7 Project Team
 *
 * Revised: Date. Name Desc
 * 2010-02-26 stlim : Create new function to print Purchase Order in PDF format
 * Joan Chia 2014-12-15 for AU-SD011432058 - GST enhancement
 *
 */
package qrcom.PROFIT.reports.BY;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import qrcom.PROFIT.files.info.ClassmstSQL;
import qrcom.PROFIT.files.info.ItemmstInfo;
import qrcom.PROFIT.files.info.ItemmstSQL;
import qrcom.PROFIT.files.info.AduserSQL;
import qrcom.PROFIT.files.info.PodetSQL;
import qrcom.PROFIT.files.info.PohdrSQL;
import qrcom.PROFIT.files.info.PostrdetSQL;
import qrcom.PROFIT.files.info.ProfitvvSQL;
import qrcom.PROFIT.files.info.RecdetInfo;
import qrcom.PROFIT.files.info.RecdetSQL;
import qrcom.PROFIT.files.info.RectxstrInfo;
import qrcom.PROFIT.files.info.RectxstrSQL;
import qrcom.PROFIT.files.info.StrmstInfo;
import qrcom.PROFIT.files.info.StrmstSQL;
import qrcom.PROFIT.files.info.SysctlSQL;
import qrcom.PROFIT.files.info.UnitcmstSQL;
import qrcom.PROFIT.files.info.UnitmstSQL;
import qrcom.PROFIT.files.info.VnmstrInfo;
import qrcom.PROFIT.files.info.VnmstrSQL;
import qrcom.PROFIT.files.info.CoysubmstSQL;
import qrcom.PROFIT.files.info.VncntermSQL;
import qrcom.PROFIT.files.info.VnbranchInfo;
import qrcom.PROFIT.files.info.VnbranchSQL;
import qrcom.PROFIT.files.info.PostrallocSQL;
import qrcom.PROFIT.files.info.RecstrallocSQL;
import qrcom.PROFIT.reports.AeonChinaA4PaperFormat;
import qrcom.PROFIT.reports.GenericReport;
import qrcom.PROFIT.shared.Utility.SellCostCalc;

import qrcom.util.HParam;
import qrcom.util.qrMath;
import qrcom.util.qrMisc;
import qrcom.util.qrRound;
import qrcom.util.CurrencyConverter;

import java.util.Map;
import java.util.Calendar;

import java.sql.*;

import java.io.*;

public class PrintPurchaseOrderSlip_Type3 extends GenericReport {
    private static final String PROGRAM_VERSION = "stlim 2010-02-26 03:52";
    private boolean test_run = true;

    private PreparedStatement psPmgrp = null;
    private PreparedStatement psBarcode = null;
    private PreparedStatement psDateRecv = null;
    private PreparedStatement psUpdPochklsthdr = null;
    private PreparedStatement psRecdet = null;
    private PreparedStatement psRectxstr = null;

    private PohdrSQL pohdrSQL = null;
    private PostrdetSQL postrdetSQL = null;
    private PodetSQL podetSQL = null;
    private PodetSQL consoPodetSQL = null;
    private VnmstrSQL vnmstrSQL = null;
    private VnbranchSQL vnbranchSQL = null;
    private UnitcmstSQL unitcmstSQL = null;
    private ItemmstSQL itemmstSQL = null;
    private ItemmstSQL consoItemmstSQL = null;
    private UnitmstSQL unitmstSQL = null;
    private StrmstSQL strmstSQL = null;
    private RecdetSQL recdetSQL = null;
    private RectxstrSQL rectxstrSQL = null;
    private ProfitvvSQL profitvvSQL = null;
    private SysctlSQL sysctlSQL = null;
    private AduserSQL aduserSQL = null;

    //added by joan for GST enhancement 2014-12-15
    private PostrallocSQL postrallocSQL = null;
    private RecstrallocSQL recstrallocSQL = null;
    //added end

    private CoysubmstSQL coysubmstSQL = null; // Add by SALOW 2013-09-05 - For qrRound Implementation .
    private VncntermSQL vncntermSQL = null; // Add by SALOW 2013-09-05 - For qrRound Implementation .
    private ClassmstSQL classmstSQL = null; //Added by Genesis 2017-06-13 SRAM019406

    private String RPTPARAM = null; // Y or N
    private String SESSION_ID = null;
    private String TIME_CD = null;
    private String ACTION = null;
    private String strCOY = null;
    private String USER_ID = null;

    private Font FontChinese_bigfont = null;
    private Font FontChinese_no = null;
    private Font FontChinese_title = null;
    private Font FontChinese_footerFont = null;

    private String strRptTitle = "";
    private String strToParty = "";
    private String strLineCode = "";
    private String prevSlipNo = "";
    private String prevStore = "";

    private String strSYSShowPoRetailInfo =
        ""; //Added by whpuah 03/05/2017 - use profitvv to control whether to display retail price or not

    private String poSupplContract = ""; // cltan
    private boolean blnConsoPrinted = false;
    private boolean blnIsPreDis = true;
    private String strDateRecv = "";

    private long lngDelvQty = 0;
    private int intCnt = 0;
    private int maxCount = 10;

    private double costTot = 0;
    private double retailTot = 0;
    private double costamount = 0;
    private double retailamount = 0;
    private double discAmount = 0;
    private double discTot = 0;
    private double poVatRate = 0;

    private int decimalDisplay = 0; // Add by SALOW 2013-09-05 - For qrRound Implementation .
    private int percentageDisplay = 0; // Add by SALOW 2013-09-05 - For qrRound Implementation .
    private int quantityDisplay = 0; // Add by SALOW 2013-09-05 - For qrRound Implementation .

    private Table tableSuperSuperHdr = null;
    private Table tableSuperHdr = null;
    private Table tableHdr = null;
    private Table tableDataHdr = null;

    private SellCostCalc sellCostCalculation = null; // Add by SALOW 2013-09-05 - For qrRound Implementation .
    private qrRound qrRound = null; // Add by SALOW 2013-09-05 - For qrRound Implementation .
    private qrRound qrRound_GST = null; // Add by SALOW 2013-09-05 - For qrRound Implementation .

    private String strReportFormat = "";
    private String prevReportFormat = "";

    private PrintPurchaseOrderSlip_Type3() {
    }

    public PrintPurchaseOrderSlip_Type3(String filename) {
        super(filename);
    }

    public PrintPurchaseOrderSlip_Type3(OutputStream outStream) {
        super(outStream);
    }

    public void print(HParam hParam) {
        try {
            super.openOutputStream();
            openConnection();
            initObjSQL();
            jInit(hParam);
            printPurchaseOrder();
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException sqle) {
            }

            e.printStackTrace();
        } finally {
            try {
                conn.commit();
            } catch (SQLException sqle) {
            }

            closeConnection();
            super.closeOutputStream();
        }
    }

    private void printPurchaseOrder() throws Exception {
        // Add by SALOW 2013-09-05 - For qrRound Implementation .
        qrRound = new qrRound(strCOY, "PO", conn);
        qrRound_GST = new qrRound(strCOY, sellCostCalculation.QRROUND_GROUP_GST, conn);

        sellCostCalculation.setQrRound(qrRound_GST);

        getDecimalDisplay(strCOY);

        percentageDisplay = qrRound.getPercRoundValue();
        quantityDisplay = qrRound.getQtyRoundValue();
        // Code End .

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Table datatable = null;
        boolean first_time = true;

        String query = "";

        String colDateCrlName = getDateCrlCol();

        if (RPTPARAM != null && RPTPARAM.equals("Y")) {
            query = getMainRptparamQuery(colDateCrlName);
        } else {
            query = getMainQuery();
        }

        pstmt = conn.prepareStatement(query);
        rs = pstmt.executeQuery();

        while (rs != null && rs.next()) {
            pohdrSQL.populate(rs);
            //added by joan for GST enhancement 2014-12-15
            if (pohdrSQL.PO_CREATE_METHOD().equals("E") || pohdrSQL.PO_CREATE_METHOD().equals("R")) {
                postrallocSQL.populate(rs);
            } else {
                postrdetSQL.populate(rs);
            }
            podetSQL.populate(rs);
            itemmstSQL.populate(rs);

            // Add by SALOW 2013-09-05 - For qrRound Implementation .
            getCOYSUBMST(pohdrSQL.COY(), pohdrSQL.COY_SUB());
            getVNCNTERM(pohdrSQL.PO_SUPPL(), pohdrSQL.PO_SUPPL_CONTRACT());
            // Code End .

            strReportFormat = rs.getString("RPT_FORMAT");

            strSYSShowPoRetailInfo =
                getProfitvv("SYSShowPoRetailInfo"); //Added by whpuah 03/05/2017 - Remove Retail Price(SRAV045085)- use profitvv to control whether to display retail price

            if ((pohdrSQL.CONSOLIDATE_FLAG() != null && !pohdrSQL.CONSOLIDATE_FLAG().equals("N")) ||
                (pohdrSQL.PO_CREATE_METHOD().equals("E") || pohdrSQL.PO_CREATE_METHOD().equals("R"))) {
                poSupplContract = null;
            }
            //if(pohdrSQL.CONSOLIDATE_FLAG().equals("Y") && !pohdrSQL.ORD_NO().equals(prevSlipNo)) {
            if (pohdrSQL.CONSOLIDATE_FLAG() != null && !pohdrSQL.CONSOLIDATE_FLAG().equals("N") &&
                !pohdrSQL.ORD_NO().equals(prevSlipNo)) {
                if (first_time == false) {
                    checkTotalLine((maxCount - intCnt), intCnt);
                    printBlankCell(1);
                    printReportFooter(intCnt);
                    intCnt = 0;
                    initTotal();
                }
                printNewPage();
                blnIsPreDis = false;
                printConsolidatePo();
                blnConsoPrinted = true;
            } else {
                blnConsoPrinted = false;
            }

            blnIsPreDis = true;
            getSlipProperty();
            tableSuperSuperHdr = createTableSuperSuperHdr();
            tableSuperHdr = createTableSuperHdr();
            tableHdr = createTableHdr();
            tableDataHdr = createDataHdrTable();

            //added by joan for GST enhancement 2014-12-15
            if (pohdrSQL.PO_CREATE_METHOD().equals("E") || pohdrSQL.PO_CREATE_METHOD().equals("R")) {
                if ((!pohdrSQL.ORD_NO().equals(prevSlipNo) || !postrallocSQL.STORE().equals(prevStore)) &&
                    first_time == false) {
                    
                    strReportFormat = prevReportFormat;
                    
                    if (blnConsoPrinted == false) {
                        checkTotalLine((maxCount - intCnt), intCnt);
                        printBlankCell(1);
                        printReportFooter(intCnt);
                    }
                    intCnt = 0;
                    initTotal();
                    printNewPage();
                    printIncreaseOneLine();
                    printHeader(tableSuperSuperHdr, tableSuperHdr, tableHdr, tableDataHdr);
                }
            } else {
                if ((!pohdrSQL.ORD_NO().equals(prevSlipNo) || !postrdetSQL.STORE().equals(prevStore)) &&
                    first_time == false) {
                    
                    strReportFormat = prevReportFormat;
                    
                    if (blnConsoPrinted == false) {
                        checkTotalLine((maxCount - intCnt), intCnt);
                        printBlankCell(1);
                        printReportFooter(intCnt);
                    }
                    intCnt = 0;
                    initTotal();
                    printNewPage();
                    printIncreaseOneLine();
                    printHeader(tableSuperSuperHdr, tableSuperHdr, tableHdr, tableDataHdr);
                }
            }
            if (first_time == true) {
                if (blnConsoPrinted == true) {
                    printNewPage();
                }

                printIncreaseOneLine();
                printHeader(tableSuperSuperHdr, tableSuperHdr, tableHdr, tableDataHdr);
                first_time = false;
            }

            if (intCnt == maxCount) {
                printBlankCell(1);
                printReportFooter(intCnt);
                printNewPage(); //start new page in order to fix max 10 record per page
                printIncreaseOneLine();
                printHeader(tableSuperSuperHdr, tableSuperHdr, tableHdr, tableDataHdr);
                intCnt = 0;
            }

            intCnt++;
            datatable = createDataTable(intCnt);
            document.add(datatable);

            getVnmstr(pohdrSQL.PO_SUPPL());
            strDateRecv = getDate(getDateRecv(pohdrSQL.COY(), pohdrSQL.ORD_NO()));

            addTotal();

            if (!prevSlipNo.equals(pohdrSQL.ORD_NO())) {
                updatePOHDR();
            }
            prevSlipNo = pohdrSQL.ORD_NO();
            poVatRate = pohdrSQL.PO_VAT_RATE();
            poSupplContract = pohdrSQL.PO_SUPPL_CONTRACT();
            
            prevReportFormat = rs.getString("RPT_FORMAT");
            
            //added by joan for GST enhancement 2014-12-15
            if (pohdrSQL.PO_CREATE_METHOD().equals("E") || pohdrSQL.PO_CREATE_METHOD().equals("R")) {
                prevStore = postrallocSQL.STORE();
            } else {
                prevStore = postrdetSQL.STORE();
            }
        } //end of While loop

        strReportFormat = prevReportFormat;
        if (first_time == true) {
            printIncreaseOneLine();
            printHeader(tableSuperSuperHdr, tableSuperHdr, tableHdr, tableDataHdr);
            printBlankPage();
            printBlankCell(1);
            printReportFooter(intCnt);
        } else {
            checkTotalLine((maxCount - intCnt), intCnt);
            printBlankCell(1);
            printReportFooter(intCnt);
            initTotal();
            intCnt = 0;
        }

        if (!IS_TEST_RUN && RPTPARAM != null && RPTPARAM.equals("Y"))
            deleteRPTPARAM();


        this.closeGlobalPstmt();

        if (rs != null) {
            rs.close();
            rs = null;
        }
        if (pstmt != null) {
            pstmt.close();
            pstmt = null;
        }

        document.close();
    }

    private String getDateCrlCol() throws Exception {
        String strDateCtrlCol = null;
        String query = "SELECT DATE_CTRL FROM taxrptctrl WHERE DOC_TYPE='Purchase Order' AND ROWNUM = 1";
        PreparedStatement pstmt = conn.prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();
        if (rs != null && rs.next()) {
            strDateCtrlCol = rs.getString("DATE_CTRL");
        }
        closePreparedStatement(pstmt);
        closeResultSet(rs);
        return strDateCtrlCol;
    }

    private void printConsolidatePo() throws Exception {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Table datatable = null;
        boolean first_time = true;
        int intCount = 0;
        getVnmstr(pohdrSQL.PO_SUPPL());
        strDateRecv = getDate(getDateRecv(pohdrSQL.COY(), pohdrSQL.ORD_NO()));

        pstmt = conn.prepareStatement(getPodetQuery());
        rs = pstmt.executeQuery();
        getSlipProperty();
        tableSuperSuperHdr = createTableSuperSuperHdr();
        tableSuperHdr = createTableSuperHdr();
        tableHdr = createTableHdr();
        tableDataHdr = createDataHdrTable();
        while (rs != null && rs.next()) {
            //podetSQL.populate(rs);
            consoPodetSQL.populate(rs);
            
            if (first_time == true) {
                printIncreaseOneLine();
                printHeader(tableSuperSuperHdr, tableSuperHdr, tableHdr, tableDataHdr);
                first_time = false;
            }

            if (intCount == maxCount) {
                printBlankCell(1);
                printReportFooter(intCount);
                printNewPage(); //start new page in order to fix max 10 record per page
                printIncreaseOneLine();
                printHeader(tableSuperSuperHdr, tableSuperHdr, tableHdr, tableDataHdr);
                intCount = 0;
            }
            intCount++;
            datatable = createConsoPoDataTable(intCount);
            document.add(datatable);

            addTotal();
        }

        if (first_time == true) {
            printIncreaseOneLine();
            printHeader(tableSuperSuperHdr, tableSuperHdr, tableHdr, tableDataHdr);
            printBlankPage();
            printBlankCell(1);
            printReportFooter(intCount);
        } else {
            //added by joan for GST enhancement
            poVatRate = pohdrSQL.PO_VAT_RATE();
            poSupplContract = pohdrSQL.PO_SUPPL_CONTRACT();
            checkTotalLine((maxCount - intCount), intCount);
            printBlankCell(1);
            printReportFooter(intCount);
            initTotal();
            intCount = 0;
        }

        this.closeGlobalPstmt();
        if (rs != null) {
            rs.close();
            rs = null;
        }
        if (pstmt != null) {
            pstmt.close();
            pstmt = null;
        }
    }

    private void closeGlobalPstmt() throws Exception {
        if (psPmgrp != null) {
            psPmgrp.close();
            psPmgrp = null;
        }
        if (psBarcode != null) {
            psBarcode.close();
            psBarcode = null;
        }
        if (psDateRecv != null) {
            psDateRecv.close();
            psDateRecv = null;
        }
        if (psUpdPochklsthdr != null) {
            psUpdPochklsthdr.close();
            psUpdPochklsthdr = null;
        }
        if (psRecdet != null) {
            psRecdet.close();
            psRecdet = null;
        }
        if (psRectxstr != null) {
            psRectxstr.close();
            psRectxstr = null;
        }
    }

    private void updatePOHDR() throws SQLException {
        sysctlSQL.setSYS_CTL_KEY("1");
        sysctlSQL.getByKey();

        pohdrSQL.getByKey();
        if (pohdrSQL.ORD_STATUS().equals("A")) {
            pohdrSQL.setORD_STATUS("R");
            pohdrSQL.setPO_RELEASE_DATE(sysctlSQL.getLAST_DAY_UPDATEDPlusOne());
            this.updatePOCHKLSTHDR();
        }
        pohdrSQL.setPO_PRINTED_CNT(pohdrSQL.PO_PRINTED_CNT() + 1);
        pohdrSQL.setLAST_OPR(USER_ID);
        pohdrSQL.setLAST_OPR_FUNCT("PRINT PO");
        pohdrSQL.update();
    }

    private void updatePOCHKLSTHDR() throws SQLException {
        if (psUpdPochklsthdr == null) {
            psUpdPochklsthdr =
                conn.prepareStatement("UPDATE POCHKLSTHDR " + "SET CHKLST_STATUS = 'R'" + ",LAST_OPR = ?" +
                                      ",LAST_OPR_FUNCT = ?" + ",LAST_OPR_DATE = ?" + ",LAST_VERSION = ? " +
                                      "WHERE COY = ? " + "AND STORE = ?" + "AND CHKLST_NO = ?");
        }
        psUpdPochklsthdr.setString(1, USER_ID);
        psUpdPochklsthdr.setString(2, "PRINT PO");
        psUpdPochklsthdr.setDate(3, new java.sql.Date(System.currentTimeMillis()));
        psUpdPochklsthdr.setLong(4, System.currentTimeMillis());
        psUpdPochklsthdr.setString(5, pohdrSQL.COY());
        psUpdPochklsthdr.setString(6, pohdrSQL.PO_STORE());
        psUpdPochklsthdr.setString(7, pohdrSQL.ORD_REFERENCE());
        psUpdPochklsthdr.executeUpdate();
    }

    private void deleteRPTPARAM() throws SQLException {
        PreparedStatement psDelRptprm = null;
        psDelRptprm = conn.prepareStatement("DELETE FROM RPTPARAM WHERE SESSION_ID=? AND TIME_CD=? ");
        psDelRptprm.setString(1, SESSION_ID);
        psDelRptprm.setString(2, TIME_CD);
        psDelRptprm.execute();
        psDelRptprm.close();
        psDelRptprm = null;
    }

    private String getMainQuery() throws SQLException {
        return "";
    }

    private String getMainRptparamQuery(String colDateCrlName) throws SQLException {
        // added by joan for GST enhancement 2014-12-15 - BEGIN get PO_CREATE_METHOD
        ResultSet rs;
        PreparedStatement ps;
        String po_create_method = "";

        String query = " SELECT P.PO_CREATE_METHOD";
        query += " FROM POHDR P, RPTPARAM R";
        query +=
            " WHERE R.SESSION_ID = '" + SESSION_ID + "' " + " AND R.TIME_CD = " + Long.valueOf(TIME_CD).longValue() +
            " " + " AND R.ACTION = '" + ACTION + "'" + " AND R.PARAM_1 = P.COY AND R.PARAM_2 = P.ORD_NO ";
        query += " AND P.COY = R.PARAM_1" + " AND P.ORD_NO = R.PARAM_2";
        query += " AND ROWNUM=1"; // assuming SESSION_ID+TIME_CD guarantees uniqueness

        ps = conn.prepareStatement(query);
        rs = ps.executeQuery();

        if (rs != null && rs.next())
            po_create_method = rs.getString("PO_CREATE_METHOD");
        // END get PO_CREATE_METHOD

        String selectMoreColumn = "";
        String leftJoinTaxrptCtrl = "";
        if (colDateCrlName != null) {
            selectMoreColumn = ", Nvl(R.RPT_FORMAT,2) RPT_FORMAT ";
            leftJoinTaxrptCtrl =
                " Left JOIN TAXRPTCTRL R ON POHDR." + colDateCrlName + " BETWEEN R.FR_UPDATED_DT " +
                " AND R.TO_UPDATED_DT AND R.DOC_TYPE='Purchase Order'";
        }

        String expression = "";
        // added by joan for GST enhancement 2014-12-15
        query = "";
        if (po_create_method.equals("E") || po_create_method.equals("R")) // condition
        {
            query = " SELECT POHDR.*, PODET.*, ITEMMST.*, POSTRALLOC.* " + selectMoreColumn + // added postralloc
                " FROM POHDR" + leftJoinTaxrptCtrl + ", PODET, ITEMMST, POSTRALLOC ";
        } else {
            query =
                " SELECT POHDR.*, POSTRDET.*, PODET.*, ITEMMST.* " + selectMoreColumn + " FROM POHDR" +
                leftJoinTaxrptCtrl + ", POSTRDET, PODET, ITEMMST ";
        }


        /* remark by CHUA @ 2011-03-16 - fine tune query
    String where =  " WHERE POHDR.COY IN (SELECT RPTPARAM.PARAM_1 FROM RPTPARAM " +
                                        " WHERE RPTPARAM.SESSION_ID = '" + SESSION_ID + "' " +
                                        " AND RPTPARAM.TIME_CD = " + Long.valueOf(TIME_CD).longValue() + " " +
                                        " AND RPTPARAM.ACTION = '" + ACTION + "' ) " +
                    " AND POHDR.ORD_NO IN (SELECT RPTPARAM.PARAM_2 FROM RPTPARAM " +
                                         " WHERE RPTPARAM.SESSION_ID = '" + SESSION_ID + "' " +
                                         " AND RPTPARAM.TIME_CD = " + Long.valueOf(TIME_CD).longValue() + " " +
                                         " AND RPTPARAM.ACTION = '" + ACTION + "' ) " +
    */

        String where =
            " WHERE EXISTS (SELECT 1 FROM RPTPARAM " + " WHERE RPTPARAM.SESSION_ID = '" + SESSION_ID + "' " +
            " AND RPTPARAM.TIME_CD = " + Long.valueOf(TIME_CD).longValue() + " " + " AND RPTPARAM.ACTION = '" + ACTION +
            "' AND RPTPARAM.PARAM_1 = POHDR.COY AND RPTPARAM.PARAM_2 = POHDR.ORD_NO) " +
            " AND PODET.COY =  POHDR.COY " + //added by joan for GST enhancement 2014-12-15
            " AND PODET.ORD_NO =  POHDR.ORD_NO " + //added by joan for GST enhancement 2014-12-15
            " AND PODET.SHORT_SKU = ITEMMST.SHORT_SKU ";
        //added by joan for GST enhancement 2014-12-15
        if (po_create_method.equals("E") || po_create_method.equals("R")) {
            where +=
                " AND POSTRALLOC.COY = PODET.COY " + " AND POSTRALLOC.ORD_NO = PODET.ORD_NO " +
                " AND POSTRALLOC.SHORT_SKU = PODET.SHORT_SKU " + " AND POSTRALLOC.PO_SEQ = PODET.PO_SEQ ";
        } else {
            where +=
                " AND POSTRDET.COY = PODET.COY " + " AND POSTRDET.ORD_NO = PODET.ORD_NO " +
                " AND POSTRDET.SHORT_SKU = PODET.SHORT_SKU " + " AND POSTRDET.PO_SEQ = PODET.PO_SEQ ";
        }

        String orderBy = "";
        if (po_create_method.equals("E") || po_create_method.equals("R")) {
            orderBy += " ORDER BY POSTRALLOC.COY, POSTRALLOC.ORD_NO desc, POSTRALLOC.STORE , POSTRALLOC.SHORT_SKU ";
        } else {
            orderBy += " ORDER BY POSTRDET.COY, POSTRDET.ORD_NO desc, POSTRDET.STORE , POSTRDET.SHORT_SKU ";
        }
        //added end
        query = query + where + expression + orderBy;
        //System.out.println("SELECT QUERY : " + query);
        return query;
    }

    private String getPodetQuery() throws SQLException {
        String expression = "";

        String query = "SELECT PODET.* " + "FROM POHDR, PODET ";
        String where =
            "WHERE POHDR.COY = '" + pohdrSQL.COY() + "' " + "AND POHDR.ORD_NO = '" + pohdrSQL.ORD_NO() + "' " +
            "AND POHDR.COY = PODET.COY " + "AND POHDR.ORD_NO = PODET.ORD_NO ";
        String orderBy = "ORDER BY POHDR.COY, POHDR.ORD_NO, PODET.SHORT_SKU";

        query = query + where + expression + orderBy;
        return query;
    }

    private void printNewPage() throws Exception {
        document.newPage();
    }

    private void printHeader(Table hdr1, Table hdr2, Table hdr3, Table hdr4) throws Exception {
        document.add(hdr1);
        printBlankCell(1);
        document.add(hdr2);
        printBlankCell(1);
        document.add(hdr3);
        printBlankCell(1);
        document.add(hdr4);
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

    private void printReportFooter(int _cnt) throws Exception {
        //Table footerTable = createTableFooter();
        //document.add(footerTable);
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

        for (int i = 0; i < max; i++)
            tableBlank.addCell(cell);
        document.add(tableBlank);
    }

    private void printBlankPage() throws Exception {
        Table blankTable = null;
        int j = 0;
        for (j = 0; j < maxCount; j++) {
            blankTable = createBlankTable(0);
            document.add(blankTable);
        }

        Table createTotalTable = null;
        createTotalTable = createTotalTable();
        document.add(createTotalTable);
    }

    private void checkTotalLine(int rows_to_print, int prmCurrCnt) throws Exception {
        Table blankTable = null;
        Table createTotalTable = null;
        int j = 0;
        for (j = 0; j < rows_to_print; j++) {
            ++prmCurrCnt;
            blankTable = createBlankTable(prmCurrCnt);
            document.add(blankTable);
        }
        createTotalTable = createTotalTable();
        document.add(createTotalTable);
    }

    private Table createTotalTable() throws Exception {
        // Change by SALOW 2013-08-19 - For AEON ASEAN changes .
        Table tableTotal = new Table(13);
        int headerwidths[] = { 8, 20, 1, 5, 5, 5, 1, 26, 5, 6, 6, 6, 6 };

        Map resultMapCostOnly = null; // Add by SALOW 2013-09-05 - For qrRound Implementation .
        // Code End .

        tableTotal.setPadding(1);
        tableTotal.setSpacing(0);
        tableTotal.setWidths(headerwidths);
        tableTotal.setWidth(100);
        tableTotal.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
        tableTotal.setBorder(Rectangle.NO_BORDER);

        Cell cell = null;


        // Change by SALOW 2013-08-19 - For AEON ASEAN changes .

        cell = new Cell();
        cell.setColspan(8);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal.addCell(cell);

        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_TOTAL"), FontChinese));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.setColspan(2);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal.addCell(cell);

        // note change for 1.2 on the total is not necessary anymore because the total amounts for cost
        // and discounts is retrieved from the amounts divided by the exchange rate --- arvin
        double dblTotCostAmt = qrRound.roundCost(costTot); // Change by SALOW 2013-09-05 - For qrRound Implementation .
        String strTotCostAmt =
            currencyConverter.format(dblTotCostAmt).toString(); // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        cell = new Cell(new Phrase(strTotCostAmt, FontChinese));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        tableTotal.addCell(cell);

        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_TOTAL"), FontChinese));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal.addCell(cell);


        double dblTotRetailAmt =
            qrRound.roundSell(retailTot); // Change by SALOW 2013-09-05 - For qrRound Implementation .
        String strTotRetailAmt = "";
        if (strSYSShowPoRetailInfo != null &&
            strSYSShowPoRetailInfo.equals("N")) //added if else by whpuah 03/05/2017 -- display total retail price only when SYSShowPoRetailInfo = Y
        {
            strTotRetailAmt = currencyConverter.format(0).toString();
        } else {
            strTotRetailAmt =
                currencyConverter.format(dblTotRetailAmt).toString(); // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        }
        cell = new Cell(new Phrase(strTotRetailAmt, FontChinese));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        tableTotal.addCell(cell);

        if (pohdrSQL.CONSOLIDATE_FLAG().equals("N"))
            cell =
                new Cell(new Phrase(getTranslatedReportMsg("SLIP_SUPPLIER_NO") + " \n" +
                                    getTranslatedReportMsg("SLIP_CONTRACT"), FontChinese));
        else if (poSupplContract != null && !(blnIsPreDis))
            cell =
                new Cell(new Phrase(getTranslatedReportMsg("SLIP_SUPPLIER_NO") + " \n" +
                                    getTranslatedReportMsg("SLIP_CONTRACT"), FontChinese));
        else
            cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_SUPPLIER_NO"), FontChinese));
        tableTotal.addCell(cell);


        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_SUPPLIER_NAME"), FontChinese));
        tableTotal.addCell(cell);


        cell = new Cell(new Phrase("", FontChinese));
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal.addCell(cell);


        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_DATE_RECEIVED"), FontChinese));
        cell.setColspan(3);
        tableTotal.addCell(cell);

        cell = new Cell(new Phrase("", FontChinese));
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal.addCell(cell);

        //added by joan for GST enhancement 2014-12-16
        String msg = "";
        if (pohdrSQL.PO_CREATE_METHOD().equals("E") || pohdrSQL.PO_CREATE_METHOD().equals("R")) //Remark
        {
            msg = getTranslatedReportMsg("SLIP_SORTING_QUANTITY_BY_RETAIL_UOM");
        }
        //Added by Genesis 2017-06-13 SRAM019406
        else {
            classmstSQL.setCLASS(pohdrSQL.PO_GROUP());
            if (classmstSQL.getByKey() > 0) {
                if (classmstSQL.REMARK_PO() != null && classmstSQL.REMARK_PO().equals("Y")) {
                    msg = getTranslatedReportMsg(pohdrSQL.REMARK());
                }
            }
        }
        //Done Added by Genesis 2017-06-13 SRAM019406

        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_REMARK") + "\n" + msg, FontChinese));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.enableBorderSide(Rectangle.BOX);
        cell.setRowspan(3);
        tableTotal.addCell(cell);

        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_TOTAL_DISCOUNT"), FontChinese));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.setColspan(2);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal.addCell(cell);


        double dblTotDiscAmt = qrRound.roundCost(discTot); // Change by SALOW 2013-09-05 - For qrRound Implementation .
        String strTotDiscAmt =
            currencyConverter.format(dblTotDiscAmt).toString(); // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        cell = new Cell(new Phrase(strTotDiscAmt, FontChinese));

        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        tableTotal.addCell(cell);


        cell = new Cell(new Phrase(getTranslatedReportMsg(""), FontChinese));

        cell.setColspan(2);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal.addCell(cell);
        if (pohdrSQL.CONSOLIDATE_FLAG().equals("N")) {
            if (coysubmstSQL.COY_PYMT_MTD().equals("2")) {
                cell =
                    new Cell(new Phrase(vnmstrSQL.VN_CODE() + "\n" + pohdrSQL.PO_SUPPL_CONTRACT() + "\n" +
                                        pohdrSQL.PO_SUPPL_BRANCH(), FontChinese)); //Supplier No  // cltan
            } else {
                cell =
                    new Cell(new Phrase(vnmstrSQL.VN_CODE() + "\n" + poSupplContract,
                                        FontChinese)); //Supplier No  // cltan
            }
        } else if (poSupplContract != null && !(blnIsPreDis)) {
            if (coysubmstSQL.COY_PYMT_MTD().equals("2")) {
                cell =
                    new Cell(new Phrase(vnmstrSQL.VN_CODE() + "\n" + poSupplContract + "\n" +
                                        pohdrSQL.PO_SUPPL_BRANCH(), FontChinese)); //Supplier No  // cltan
            } else {
                cell =
                    new Cell(new Phrase(vnmstrSQL.VN_CODE() + "\n" + poSupplContract,
                                        FontChinese)); //Supplier No  // cltan
            }
        } else {
            if (coysubmstSQL.COY_PYMT_MTD().equals("2")) {
                cell =
                    new Cell(new Phrase(vnmstrSQL.VN_CODE() + "\n" + pohdrSQL.PO_SUPPL_BRANCH(),
                                        FontChinese)); //Supplier No  // cltan
            } else {
                cell = new Cell(new Phrase(vnmstrSQL.VN_CODE(), FontChinese)); //Supplier No  // cltan
            }
        }

        cell.setRowspan(2);
        tableTotal.addCell(cell);

        if (coysubmstSQL.COY_PYMT_MTD().equals("2")) {
            getVnbranch(pohdrSQL.PO_SUPPL(), pohdrSQL.PO_SUPPL_BRANCH());
            cell = new Cell(new Phrase(getDescription(vnbranchSQL.BRANCH_NAME()), FontChinese)); //Supplier Name
        } else // then it should be equals to "1"
        {
            cell = new Cell(new Phrase(getDescription(vnmstrSQL.VN_NAME()), FontChinese)); //Supplier Name
        }

        cell.setRowspan(2);
        tableTotal.addCell(cell);

        cell = new Cell(new Phrase(getTranslatedReportMsg(""), FontChinese));

        cell.setRowspan(2);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal.addCell(cell);

        //Date Received
        cell = new Cell(new Phrase(strDateRecv.length() > 0 ? strDateRecv.substring(0, 4) : "", FontChinese));

        cell.setRowspan(2);
        tableTotal.addCell(cell);
        cell = new Cell(new Phrase(strDateRecv.length() > 0 ? strDateRecv.substring(5, 7) : "", FontChinese));

        cell.setRowspan(2);
        tableTotal.addCell(cell);
        cell = new Cell(new Phrase(strDateRecv.length() > 0 ? strDateRecv.substring(8, 10) : "", FontChinese));

        cell.setRowspan(2);
        tableTotal.addCell(cell);
        cell = new Cell(new Phrase(getTranslatedReportMsg(""), FontChinese));

        cell.setRowspan(2);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal.addCell(cell);

        cell = new Cell(new Phrase("NET TOTAL", FontChinese)); //edit by joan for GST enhancement

        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.setColspan(2);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal.addCell(cell);

        double dblNetTotWithVat =
            dblTotCostAmt - dblTotDiscAmt; // Change by SALOW 2013-09-27 - For Currency Converter Implementation .

        String strNetTotWithVat =
            currencyConverter.format(dblNetTotWithVat).toString(); // Change by SALOW 2013-09-27 - For Currency Converter Implementation .

        cell = new Cell(new Phrase(strNetTotWithVat, FontChinese));

        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        tableTotal.addCell(cell);

        cell = new Cell(new Phrase("", FontChinese));
        cell.setColspan(2);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal.addCell(cell);

        String strGstAmount;
        String strTotVAT;
        String strNetTotalGst;
        String strCostTotWithVAT;
        
        if (strReportFormat.equals("1")) {
            strGstAmount = "GST AMOUNT";
            resultMapCostOnly =
                sellCostCalculation.getCostOnlyFormula(dblNetTotWithVat, poVatRate, pohdrSQL.COY(), pohdrSQL.COY_SUB(),
                                                       coysubmstSQL.COST_TYPE(), vncntermSQL.VAT_FORMULA());
            double dblTotVAT = Double.parseDouble((String) resultMapCostOnly.get(SellCostCalc.COST_WVAT));
            double dblNewTotVAT = dblTotVAT - dblNetTotWithVat;
            strTotVAT = currencyConverter.format(dblNewTotVAT).toString();

            strNetTotalGst = "NET TOTAL INCL. GST";
            strCostTotWithVAT = currencyConverter.format(dblTotVAT).toString();
        } else {
            strGstAmount = "\n";
            strTotVAT = "\n";
            strNetTotalGst = "\n";
            strCostTotWithVAT = "\n";
        }

        cell = new Cell(new Phrase(strGstAmount, FontChinese)); //edit by joan for GST enhancement
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.setColspan(2);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal.addCell(cell);

        cell = new Cell(new Phrase(strTotVAT, FontChinese));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        tableTotal.addCell(cell);

        cell = new Cell(new Phrase("", FontChinese));
        cell.setColspan(2);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal.addCell(cell);

        cell = new Cell(new Phrase("\n", FontChinese_no));
        cell.setColspan(8);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal.addCell(cell);

        // Add by cstan - MY GST Phase 1
        cell = new Cell(new Phrase(strNetTotalGst, FontChinese)); //edit by joan for GST enhancement
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.setColspan(2);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal.addCell(cell);

        cell = new Cell(new Phrase(strCostTotWithVAT, FontChinese));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        tableTotal.addCell(cell);

        cell = new Cell(new Phrase("\n", FontChinese_no));
        cell.setColspan(2);
        cell.disableBorderSide(Rectangle.BOX);
        tableTotal.addCell(cell);
        // Code End .
        return tableTotal;
    }

    private Table createBlankTable(int prmCnt) throws Exception {
        // Change by SALOW 2013-08-19 - For AEON ASEAN changes .
        Table tableBlank = new Table(14);
        int headerwidths[] = { 3, 22, 7, 7, 9, 7, 5, 5, 6, 5, 6, 6, 6, 6 };
        // Code End .
        tableBlank.setPadding(0.5f);
        tableBlank.setSpacing(0);
        tableBlank.setWidths(headerwidths);
        tableBlank.setWidth(100);
        tableBlank.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
        tableBlank.setBorder(Rectangle.NO_BORDER);

        Cell cell = null;
        if (prmCnt > 0) {
            cell = new Cell(new Phrase(String.valueOf(prmCnt), FontChinese_no)); //No.
            cell.setRowspan(2);
            tableBlank.addCell(cell);
        } else {
            cell = new Cell(new Phrase("\n", FontChinese_no)); //No.
            cell.setRowspan(2);
            tableBlank.addCell(cell);
        }
        cell = new Cell(new Phrase("\n", FontChinese));
        cell.setRowspan(2);
        tableBlank.addCell(cell);
        cell = new Cell(new Phrase("\n", FontChinese)); //QTY / CASE
        tableBlank.addCell(cell);

        cell = new Cell(new Phrase("\n", FontChinese)); //UOM desc. ?itemmstSQL.RETAIL_UOM_DESC?
        tableBlank.addCell(cell);

        cell = new Cell(new Phrase("\n", FontChinese)); //ITEM NO
        tableBlank.addCell(cell);

        cell = new Cell(new Phrase("\n", FontChinese)); //Item Barcode
        cell.setRowspan(2);
        tableBlank.addCell(cell);
        cell = new Cell(new Phrase("\n", FontChinese)); //Qty Order
        cell.setRowspan(2);
        tableBlank.addCell(cell);

        cell = new Cell(new Phrase("\n", FontChinese)); //Delivery Qty --to be checked out
        cell.setRowspan(2);
        tableBlank.addCell(cell);

        cell = new Cell(new Phrase("\n", FontChinese)); //Discount --to be confirmed
        cell.setRowspan(2);
        tableBlank.addCell(cell);

        cell = new Cell(new Phrase("\n", FontChinese)); //Selling price --to be confirmed
        cell.setRowspan(2);
        tableBlank.addCell(cell);
        // Change by SALOW 2013-08-19 - For AEON ASEAN changes .
        //COST - Unit Price
        cell = new Cell(new Phrase("\n", FontChinese));
        cell.setRowspan(2);
        tableBlank.addCell(cell);

        //COST - Amount
        cell = new Cell(new Phrase("\n", FontChinese));
        cell.setRowspan(2);
        tableBlank.addCell(cell);

        //RETAIL - Unit Price
        cell = new Cell(new Phrase("\n", FontChinese));
        cell.setRowspan(2);
        tableBlank.addCell(cell);

        //RETAIL - Amount
        cell = new Cell(new Phrase("\n", FontChinese));
        cell.setRowspan(2);
        tableBlank.addCell(cell);
        // Code End .

        //Next Row
        cell = new Cell(new Phrase("\n", FontChinese)); //Size
        tableBlank.addCell(cell);

        cell = new Cell(new Phrase("\n", FontChinese)); //Color
        tableBlank.addCell(cell);
        cell = new Cell(new Phrase("\n", FontChinese)); //Supplier Item No
        tableBlank.addCell(cell);
        return tableBlank;
    }

    //arvin in here, we are sure that the sum are all divided by ex rate because it has already been
    // changed at the top before adding the total
    private void addTotal() throws Exception {
        costTot += costamount;
        retailTot += retailamount;
        discTot += discAmount;
    }

    private void initTotal() throws Exception {
        costTot = 0;
        retailTot = 0;
        discTot = 0;
    }

    private void initObjSQL() throws SQLException {
        pohdrSQL = new PohdrSQL(conn);
        postrdetSQL = new PostrdetSQL(conn);
        podetSQL = new PodetSQL(conn);
        vnmstrSQL = new VnmstrSQL(conn);
        vnbranchSQL = new VnbranchSQL(conn);
        unitcmstSQL = new UnitcmstSQL(conn);
        recdetSQL = new RecdetSQL(conn);
        rectxstrSQL = new RectxstrSQL(conn);
        consoPodetSQL = new PodetSQL(conn);
        consoItemmstSQL = new ItemmstSQL(conn);
        coysubmstSQL = new CoysubmstSQL(conn);
        aduserSQL = new AduserSQL(conn);

        vncntermSQL = new VncntermSQL(conn);
        itemmstSQL = new ItemmstSQL(conn);
        unitmstSQL = new UnitmstSQL(conn);
        strmstSQL = new StrmstSQL(conn);
        profitvvSQL = new ProfitvvSQL(conn);
        sysctlSQL = new SysctlSQL(conn);
        classmstSQL = new ClassmstSQL(conn); //Added by Genesis 2017-06-13 SRAM019406

        sellCostCalculation = new SellCostCalc(conn);
        //added by joan for GST enhancement 2014-12-15
        postrallocSQL = new PostrallocSQL(conn);
        recstrallocSQL = new RecstrallocSQL(conn);
    }

    private void jInit(HParam hParam) throws Exception {
        //document = new Document(PageSize.A4.rotate(),5,5,33,17); // margin: left, right, top, bottom
        AeonChinaA4PaperFormat PaperFormat = new AeonChinaA4PaperFormat();
        document = new Document(PaperFormat, 5, 5, 15, 15); // margin: left, right, top, bottom

        // retrieve the values from the URL
        USER_ID = hParam.getString("User_ID");
        strCOY = hParam.getString("COY");
        RPTPARAM = hParam.getString("RPTPARAM");

        if (RPTPARAM != null && RPTPARAM.equals("Y")) // means retrieve rptparam table
        {
            SESSION_ID = hParam.getString("SESSION_ID");
            TIME_CD = hParam.getString("TIME_CD");
            ACTION = hParam.getString("ACTION");
        }

        // user_lang = retrieveUserLanguage(USER_ID);
        setLanguageByProfitvvOrUserLanguage("SYSSlipLanguageMtd", "SYSSlipLanguage",
                                            USER_ID); // set language code used by getTranslatedReportMsg

        FontChinese_title = new Font(BASE_FONT_Chinese, 11, Font.BOLD);
        FontChinese_bigfont = new Font(BASE_FONT_Chinese, 12, Font.BOLD);
        FontChinese_footerFont = new Font(BASE_FONT_Chinese, 13, Font.BOLD);
        FontChinese_no = new Font(BASE_FONT_Chinese, 8, Font.BOLD);
        FontChinese = new Font(BASE_FONT_Chinese, 8, Font.NORMAL);

        // creation of the different writers
        pdfwriter = PdfWriter.getInstance(document, outStream); // MUST place this line right after the new Document(...
        setGroupingForReport(strCOY); // Add by SALOW 2013-09-27 - For Currency Converter Implementation .
        document.open();
    }

    private Table createTableSuperSuperHdr() throws BadElementException, DocumentException, SQLException, Exception {
        Table tableCompLogo = new Table(5);
        int headerwidths[] = { 40, 19, 23, 5, 13 };

        tableCompLogo.setPadding(1);
        tableCompLogo.setSpacing(0);
        tableCompLogo.setWidths(headerwidths);
        tableCompLogo.setWidth(100);
        tableCompLogo.setBorder(Rectangle.NO_BORDER);

        Cell cell = null;

        String coy_logo_image_file = "";
        if (IS_TEST_RUN)
            coy_logo_image_file = "/oc4j10gMy/j2ee/home/applib/images/CompanyLogo.jpg"; // commented for testing purposes
        else
            coy_logo_image_file = getProfitvv("SYSCompanyLogo"); // commented for testing purposes

        Image company_logo_image = null;
        try {
            company_logo_image = Image.getInstance(coy_logo_image_file);
            company_logo_image.scalePercent(60);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error logo " + e.toString());
        }

        if (company_logo_image != null)
            cell = new Cell(company_logo_image);
        else
            cell = new Cell(new Phrase(""));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        tableCompLogo.addCell(cell);
        cell = new Cell(new Phrase(getProfitvv("SYSCompanyName"), FontChinese_bigfont)); //Company Name
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setVerticalAlignment(PdfPCell.ALIGN_BOTTOM);
        tableCompLogo.addCell(cell);
        cell = new Cell(new Phrase(getProfitvv("SYSCompanyRegNo"), FontChinese)); //Company Registration No.
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_BOTTOM);
        tableCompLogo.addCell(cell);
        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_PO_NO"), FontChinese));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.enableBorderSide(Rectangle.BOX);
        cell.disableBorderSide(Rectangle.RIGHT);
        tableCompLogo.addCell(cell);

        cell = new Cell(new Phrase(pohdrSQL.ORD_NO(), FontChinese_footerFont));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.enableBorderSide(Rectangle.BOX);
        cell.disableBorderSide(Rectangle.LEFT);
        tableCompLogo.addCell(cell);
        return tableCompLogo;
    }

    private Table createTableSuperHdr() throws BadElementException, DocumentException, SQLException, Exception {
        Table tableSuperHdr = new Table(8);
        int headerwidths[] = { 10, 20, 40, 6, 1, 5, 9, 9 };

        tableSuperHdr.setPadding(2);
        tableSuperHdr.setSpacing(0);
        tableSuperHdr.setWidths(headerwidths);
        tableSuperHdr.setWidth(100);
        tableSuperHdr.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
        tableSuperHdr.setBorder(Rectangle.NO_BORDER);

        Cell cell = null;

        cell = new Cell(new Phrase(getTranslatedReportMsg(""), FontChinese));
        cell.disableBorderSide(Rectangle.BOX);
        tableSuperHdr.addCell(cell);

        cell = new Cell(new Phrase("", FontChinese));
        cell.setRowspan(2);
        cell.disableBorderSide(Rectangle.BOX);
        tableSuperHdr.addCell(cell);

        cell = new Cell(new Phrase(strRptTitle, FontChinese_title));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.disableBorderSide(Rectangle.BOX);
        tableSuperHdr.addCell(cell);

        // Add by SALOW 2013-08-19 - For AEON ASEAN changes .
        String slip_vat_rate;
        String po_vat_rate;
        if (strReportFormat.equals("1")) {
            slip_vat_rate = getTranslatedReportMsg("SLIP_VAT_RATE");
            po_vat_rate =
                pohdrSQL.PO_VAT_CODE() + " - " + currencyConverter.sformat(pohdrSQL.PO_VAT_RATE(), percentageDisplay);
        } else {
            slip_vat_rate = "";
            po_vat_rate = "";
        }
        cell = new Cell(new Phrase(slip_vat_rate, FontChinese));
        tableSuperHdr.addCell(cell);


        cell = new Cell(new Phrase(getTranslatedReportMsg(""), FontChinese));
        cell.disableBorderSide(Rectangle.BOX);
        tableSuperHdr.addCell(cell);
        // Code End .
        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_REPRINT"), FontChinese));
        tableSuperHdr.addCell(cell);

        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_REPRINT_DATE"), FontChinese));
        tableSuperHdr.addCell(cell);
        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_REPRINT_TIME"), FontChinese));
        tableSuperHdr.addCell(cell);

        //new row start
        cell = new Cell(new Phrase("", FontChinese_footerFont));
        cell.setRowspan(2);
        cell.disableBorderSide(Rectangle.BOX);
        tableSuperHdr.addCell(cell);
        cell = new Cell(new Phrase(strToParty, FontChinese));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setRowspan(2);
        tableSuperHdr.addCell(cell);

        // Add by SALOW 2013-08-19 - For AEON ASEAN changes .
        cell =
            new Cell(new Phrase(po_vat_rate,
                                FontChinese)); // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        cell.setRowspan(2);
        tableSuperHdr.addCell(cell);

        cell = new Cell(new Phrase("", FontChinese));

        cell.disableBorderSide(Rectangle.BOX);
        cell.setRowspan(2);
        tableSuperHdr.addCell(cell);
        // Code End .

        if (pohdrSQL.PO_PRINTED_CNT() > 0)
            cell = new Cell(new Phrase("Y", FontChinese));
        else
            cell = new Cell(new Phrase("N", FontChinese));

        cell.setRowspan(2);
        tableSuperHdr.addCell(cell);

        cell = new Cell(new Phrase(getDate(qrMisc.getSqlSysDate()), FontChinese));
        cell.setRowspan(2);
        tableSuperHdr.addCell(cell);

        Calendar cal = Calendar.getInstance();
        String time =
            qrMisc.leftFillZero(String.valueOf(cal.get(Calendar.HOUR_OF_DAY)), 2) + ":" +
            qrMisc.leftFillZero(String.valueOf(cal.get(Calendar.MINUTE)), 2);
        cell = new Cell(new Phrase(time, FontChinese));
        cell.setRowspan(2);
        tableSuperHdr.addCell(cell);

        return tableSuperHdr;
    }

    private Table createTableHdr() throws BadElementException, DocumentException, SQLException {
        Table tableHeader = new Table(13);
        int headerwidths[] = { 8, 18, 7, 18, 7, 7, 17, 3, 3, 3, 3, 3, 3 };

        tableHeader.setPadding(2);
        tableHeader.setSpacing(0);
        tableHeader.setWidths(headerwidths);
        tableHeader.setWidth(100);
        tableHeader.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
        tableHeader.setBorder(Rectangle.NO_BORDER);
        Cell cell = null;

        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_SLIP_TYPE"), FontChinese));
        tableHeader.addCell(cell);

        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_AUTHORIZED_BY"), FontChinese));
        tableHeader.addCell(cell);

        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_STORE_CODE"), FontChinese));
        tableHeader.addCell(cell);
        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_STORE_NAME"), FontChinese));
        tableHeader.addCell(cell);
        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_LINE_CODE"), FontChinese));
        tableHeader.addCell(cell);
        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_DEPT_CODE"), FontChinese));
        tableHeader.addCell(cell);

        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_DELIVERY_TO"), FontChinese));
        tableHeader.addCell(cell);

        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_ORDER_DATE"), FontChinese));
        cell.setColspan(3);
        tableHeader.addCell(cell);

        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_DELIVERY_DATE"), FontChinese));
        cell.setColspan(3);
        tableHeader.addCell(cell);

        cell = new Cell(new Phrase("321", FontChinese_footerFont)); //Slip Type
        tableHeader.addCell(cell);

        cell =
            new Cell(new Phrase(pohdrSQL.PO_AUTHORISE_BY() + " / " + getAduser(pohdrSQL.PO_AUTHORISE_BY()),
                                FontChinese)); //Authorized By
        tableHeader.addCell(cell);

        //if(pohdrSQL.CONSOLIDATE_FLAG().equals("Y") && blnIsPreDis == false) {
        if (pohdrSQL.CONSOLIDATE_FLAG() != null && !pohdrSQL.CONSOLIDATE_FLAG().equals("N") && blnIsPreDis == false) {
            cell = new Cell(new Phrase(pohdrSQL.PO_STORE(), FontChinese)); //Store Code
            getStrmst(pohdrSQL.PO_STORE());
        }
        //added by joan for GST enhancement -2014-12-15
        else {
            if (pohdrSQL.PO_CREATE_METHOD().equals("E") || pohdrSQL.PO_CREATE_METHOD().equals("R")) {
                cell = new Cell(new Phrase(postrallocSQL.STORE(), FontChinese)); //Store Code
                getStrmst(postrallocSQL.STORE());
            } else {
                cell = new Cell(new Phrase(postrdetSQL.STORE(), FontChinese)); //Store Code
                getStrmst(postrdetSQL.STORE());
            }
        }
        tableHeader.addCell(cell);
        cell =
            new Cell(new Phrase(getDescription(strmstSQL.STORE_NAME()),
                                FontChinese)); //Store Name // strmstSQL was set with postralloc.store or postrdet.store according to po_create_method
        tableHeader.addCell(cell);

        getPmgrp(pohdrSQL.PO_GROUP());
        cell = new Cell(new Phrase(strLineCode, FontChinese)); //Line Code
        tableHeader.addCell(cell);
        cell = new Cell(new Phrase(pohdrSQL.PO_GROUP(), FontChinese)); //Dept Code
        tableHeader.addCell(cell);

        String strDelvLoc = "";

        //if(pohdrSQL.CONSOLIDATE_FLAG()!=null && !pohdrSQL.CONSOLIDATE_FLAG().equals("N"))
        //{
        if (pohdrSQL.DELIVER_TO_DC().equals("Y")) {
            strDelvLoc = getTranslatedReportMsg("SLIP_DC") + " " + pohdrSQL.DELV_DC_CODE();
            getStrmst(pohdrSQL.DELV_DC_CODE());
        } else if (pohdrSQL.DELV_TO_STORE() != null && pohdrSQL.DELV_TO_STORE().length() > 0) {
            strDelvLoc = getTranslatedReportMsg("SLIP_DC") + " " + pohdrSQL.DELV_TO_STORE();
            getStrmst(pohdrSQL.DELV_TO_STORE());
        } else {
            strDelvLoc = getTranslatedReportMsg("SLIP_STORE") + " " + pohdrSQL.PO_STORE();
            getStrmst(pohdrSQL.PO_STORE());
        }

        cell =
            new Cell(new Phrase(strDelvLoc + " " + getDescription(strmstSQL.STORE_SHORT_NAME()),
                                FontChinese)); //Delivery To
        tableHeader.addCell(cell);

        String DATE = getDate(pohdrSQL.DATE_ORDER()); //Order Date
        cell = new Cell(new Phrase(DATE.substring(0, 4), FontChinese));
        cell.setRowspan(2);
        tableHeader.addCell(cell);
        cell = new Cell(new Phrase(DATE.substring(5, 7), FontChinese));
        cell.setRowspan(2);
        tableHeader.addCell(cell);
        cell = new Cell(new Phrase(DATE.substring(8, 10), FontChinese));
        cell.setRowspan(2);
        tableHeader.addCell(cell);

        DATE = getDate(pohdrSQL.DELV_DATE1()); //Delivery Date
        cell = new Cell(new Phrase(DATE.substring(0, 4), FontChinese));
        cell.setRowspan(2);
        tableHeader.addCell(cell);
        cell = new Cell(new Phrase(DATE.substring(5, 7), FontChinese));
        cell.setRowspan(2);
        tableHeader.addCell(cell);
        cell = new Cell(new Phrase(DATE.substring(8, 10), FontChinese));
        cell.setRowspan(2);
        tableHeader.addCell(cell);
        return tableHeader;
    }

    private Table createDataHdrTable() throws Exception {
        // Change by SALOW 2013-08-19 - For AEON ASEAN changes .
        Table dataHdrTable = new Table(14);
        int headerwidths[] = { 3, 22, 7, 7, 9, 7, 5, 5, 6, 5, 6, 6, 6, 6 };
        // Code End .

        dataHdrTable.setPadding(1f);
        dataHdrTable.setSpacing(0);
        dataHdrTable.setWidths(headerwidths);
        dataHdrTable.setWidth(100);
        dataHdrTable.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
        dataHdrTable.setBorder(Rectangle.NO_BORDER);

        Cell cell = null;

        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_NO"), FontChinese));
        cell.setRowspan(2);
        dataHdrTable.addCell(cell);
        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_ITEM_DESCRIPTION"), FontChinese));
        cell.setRowspan(2);
        dataHdrTable.addCell(cell);
        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_QTY_CASE"), FontChinese));
        dataHdrTable.addCell(cell);
        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_UOM"), FontChinese));
        dataHdrTable.addCell(cell);

        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_ITEM_NO"), FontChinese));
        dataHdrTable.addCell(cell);
        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_ITEM_BARCODE"), FontChinese));
        cell.setRowspan(2);
        dataHdrTable.addCell(cell);

        //added by joan for GST enhancement
        if (pohdrSQL.PO_CREATE_METHOD().equals("E") || pohdrSQL.PO_CREATE_METHOD().equals("R")) {
            cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_QTY_ORDER_RETAIL_UOM"), FontChinese));
        } else {
            cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_QTY_ORDER"), FontChinese));
        }
        //cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_QTY_ORDER"),FontChinese));
        cell.setRowspan(2);
        dataHdrTable.addCell(cell);

        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_DELIVERY_QTY"), FontChinese));
        cell.setRowspan(2);
        dataHdrTable.addCell(cell);

        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_DISCOUNT"), FontChinese));
        cell.setRowspan(2);
        dataHdrTable.addCell(cell);

        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_SELLING_PRICE"), FontChinese));
        cell.setRowspan(2);
        dataHdrTable.addCell(cell);
        //arvin
        String indentCcyCode = "";
        String localCcyCode = " (" + coysubmstSQL.FRGN_CRNCY_CD() + ")";
        if (pohdrSQL.PO_ORD_SOURCE().equals("I")) {
            indentCcyCode = " (" + pohdrSQL.PO_FRGN_CRNCY_CD() + ")";
        } else {
            indentCcyCode = " (" + coysubmstSQL.FRGN_CRNCY_CD() + ")";
        }

        String strCost;
        String strRetail;
        if (strReportFormat.equals("1")) {
            strCost = "COST (EXCL GST)" + indentCcyCode;
            strRetail = "RETAIL (INCL GST)" + localCcyCode;
        } else {
            strCost = "COST" + indentCcyCode;
            strRetail = "RETAIL" + localCcyCode;
        }

        // Change by SALOW 2013-08-19 - For AEON ASEAN changes .
        cell = new Cell(new Phrase(strCost, FontChinese)); //edited by joan for GST enhancement 2014-12-10
        // Code End .
        cell.setColspan(2);
        dataHdrTable.addCell(cell);

        cell = new Cell(new Phrase(strRetail, FontChinese)); //edited by joan for GST enhancement 2014-12-10
        cell.setColspan(2);
        dataHdrTable.addCell(cell);
        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_SIZE"), FontChinese));
        dataHdrTable.addCell(cell);

        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_COLOUR"), FontChinese));
        dataHdrTable.addCell(cell);

        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_SUPPLIER_ITEM_NO"), FontChinese));
        dataHdrTable.addCell(cell);
        String strCostUNIT = getTranslatedReportMsg("SLIP_UNIT_PRICE");
        String strCostAMOUNT = getTranslatedReportMsg("SLIP_AMOUNT");
        String strRetailUNIT = getTranslatedReportMsg("SLIP_UNIT_PRICE");
        String strRetailAMOUNT = getTranslatedReportMsg("SLIP_AMOUNT");
        cell = new Cell(new Phrase(strCostUNIT, FontChinese));
        dataHdrTable.addCell(cell);
        cell = new Cell(new Phrase(strCostAMOUNT, FontChinese));
        dataHdrTable.addCell(cell);

        cell = new Cell(new Phrase(strRetailUNIT, FontChinese));
        dataHdrTable.addCell(cell);
        cell = new Cell(new Phrase(strRetailAMOUNT, FontChinese));
        dataHdrTable.addCell(cell);
        return dataHdrTable;
    }

    private Table createDataTable(int prmCnt)
        //throws BadElementException, DocumentException ,SQLException
        throws Exception {
        // Change by SALOW 2013-08-19 - For AEON ASEAN changes .
        Table datatable = new Table(14);
        int headerwidths[] = { 3, 22, 7, 7, 9, 7, 5, 5, 6, 5, 6, 6, 6, 6 };
        // Code End .
        datatable.setPadding(0.5f);
        datatable.setSpacing(0);
        datatable.setWidths(headerwidths);
        datatable.setWidth(100);
        datatable.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
        datatable.setBorder(Rectangle.NO_BORDER);

        Cell cell = null;
        //cell = new Cell(new Phrase(qrMisc.leftFill(_cnt,2,"0"), FontChinese_no)); //No.
        cell = new Cell(new Phrase(String.valueOf(prmCnt), FontChinese_no)); //No.
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setRowspan(2);
        datatable.addCell(cell);

        //added by joan for GST enhancement 2014-12-15
        // use postrdet or postralloc according to po_create_method
        if (pohdrSQL.PO_CREATE_METHOD().equals("E") || pohdrSQL.PO_CREATE_METHOD().equals("R")) {
            getItemmst(postrallocSQL.SHORT_SKU()); // sets consoItemmstSQL
        } else {
            getItemmst(postrdetSQL.SHORT_SKU()); // sets consoItemmstSQL
        }
        //added end
        String strItemDisp = "";
        String strItem =
            getDescription(consoItemmstSQL.ITEM_DESC()); //Item Desc // consoItemmstSQL object set according to po_create_method
        if (strItem.length() > 34) {
            strItemDisp = strItem.substring(0, 34);
            cell = new Cell(new Phrase(strItemDisp, FontChinese));
        } else {
            strItemDisp = strItem;
            cell = new Cell(new Phrase(strItemDisp, FontChinese));
        }
        cell.disableBorderSide(Rectangle.BOTTOM);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        datatable.addCell(cell);

        double dblQtyPerCase = 0;
        if (!podetSQL.PO_COST_UNIT().equals(podetSQL.PO_QTY_UNIT())) {
            unitcmstSQL.setFROM_UNIT(podetSQL.PO_COST_UNIT());
            unitcmstSQL.setTO_UNIT(podetSQL.PO_QTY_UNIT());
            if (unitcmstSQL.getByKey() > 0) {
                if (unitcmstSQL.CONV_FACTOR() >= 1) {
                    dblQtyPerCase = unitcmstSQL.CONV_FACTOR();
                } else {
                    unitcmstSQL.setFROM_UNIT(podetSQL.PO_QTY_UNIT());
                    unitcmstSQL.setTO_UNIT(podetSQL.PO_COST_UNIT());
                    unitcmstSQL.getByKey();
                    dblQtyPerCase = unitcmstSQL.CONV_FACTOR();
                }
            } else
                dblQtyPerCase = 0;
        } else if (podetSQL.PO_COST_UNIT().equals(podetSQL.PO_QTY_UNIT()))
            dblQtyPerCase = 1;

        if (itemmstSQL.ITEM_PERISH_CD().equals("Y"))
            cell = new Cell(new Phrase(currencyConverter.sformat(qrRound.round(dblQtyPerCase, 2), 2), FontChinese)); //QTY / CASE // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        else
            cell =
                new Cell(new Phrase(currencyConverter.sformat(qrRound.roundQty(dblQtyPerCase), quantityDisplay),
                                    FontChinese)); //QTY / CASE // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        datatable.addCell(cell);

        cell =
            new Cell(new Phrase(getDescription(getUnitDesc(podetSQL.PO_COST_UNIT())),
                                FontChinese)); //UOM desc. ?itemmstSQL.RETAIL_UOM_DESC?
        datatable.addCell(cell);

        //added by joan for GST enhancement 2014-12-15
        if (pohdrSQL.PO_CREATE_METHOD().equals("E") || pohdrSQL.PO_CREATE_METHOD().equals("R")) {
            cell = new Cell(new Phrase(postrallocSQL.SHORT_SKU(), FontChinese)); //ITEM NO
        } else {
            cell = new Cell(new Phrase(postrdetSQL.SHORT_SKU(), FontChinese)); //ITEM NO
        }
        //cell = new Cell(new Phrase(postrdetSQL.SHORT_SKU(),FontChinese)); //ITEM NO
        datatable.addCell(cell);

        //added by joan for GST enhancement 2014-12-15
        if (pohdrSQL.PO_CREATE_METHOD().equals("E") || pohdrSQL.PO_CREATE_METHOD().equals("R")) {
            cell = new Cell(new Phrase(getBarCode(postrallocSQL.SHORT_SKU()), FontChinese)); //Item Barcode
        } else {
            cell = new Cell(new Phrase(getBarCode(postrdetSQL.SHORT_SKU()), FontChinese)); //Item Barcode
        }
        //cell = new Cell(new Phrase(getBarCode(postrdetSQL.SHORT_SKU()),FontChinese)); //Item Barcode
        cell.setRowspan(2);
        datatable.addCell(cell);

        //added by joan for GST enhancement 2014-12-15
        if (pohdrSQL.PO_CREATE_METHOD().equals("E") || pohdrSQL.PO_CREATE_METHOD().equals("R")) // condition added
        {
            if (itemmstSQL.ITEM_PERISH_CD().equals("Y"))
                cell = new Cell(new Phrase(getDouble(qrMath.round(postrallocSQL.QTY_ORDER_STORE(), 2), 2), FontChinese)); //Qty Order
            else
                cell =
                    new Cell(new Phrase(getDouble(qrMath.round(postrallocSQL.QTY_ORDER_STORE(), 0), 0), FontChinese));
        } else {
            if (itemmstSQL.ITEM_PERISH_CD().equals("Y"))
                cell =
                    new Cell(new Phrase(getDouble(qrMath.round(postrdetSQL.QTY_ORDER_STORE() *
                                                               podetSQL.COST_UNIT_FACTOR(), 2), 2),
                                        FontChinese)); //Qty Order
            else
                cell =
                    new Cell(new Phrase(getDouble(qrMath.round(postrdetSQL.QTY_ORDER_STORE() *
                                                               podetSQL.COST_UNIT_FACTOR(), 0), 0), FontChinese));
        }

        //added end
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        datatable.addCell(cell);

        //added by joan for GST enhancement 2014-12-15
        if (pohdrSQL.PO_CREATE_METHOD().equals("E") || pohdrSQL.PO_CREATE_METHOD().equals("R")) {
            getDelvQtyRecstralloc(pohdrSQL.PO_STORE(), pohdrSQL.PO_RECPT(), pohdrSQL.COY(), pohdrSQL.ORD_NO(),
                                  postrallocSQL.SHORT_SKU(), String.valueOf(postrallocSQL.PO_SEQ()),
                                  postrallocSQL.STORE());

            if (itemmstSQL.ITEM_PERISH_CD().equals("Y"))
                cell = new Cell(new Phrase(getDouble(qrMath.round(recstrallocSQL.QTY_RECV_STR_IN_COST_UNIT(), 2), 2), FontChinese)); //Delivery Qty
            else
                cell =
                    new Cell(new Phrase(getDouble(qrMath.round(recstrallocSQL.QTY_RECV_STR_IN_COST_UNIT(), 0), 0),
                                        FontChinese)); //Delivery Qty
        } else {
            //added end
            getDelvQtyRectxstr(pohdrSQL.PO_STORE(), pohdrSQL.PO_RECPT(), pohdrSQL.COY(), pohdrSQL.ORD_NO(),
                               postrdetSQL.SHORT_SKU(), String.valueOf(postrdetSQL.PO_SEQ()), postrdetSQL.STORE());
            if (itemmstSQL.ITEM_PERISH_CD().equals("Y"))
                cell =
                    new Cell(new Phrase(currencyConverter.sformat(qrRound.round(rectxstrSQL.QTY_RECV_STR_IN_COST_UNIT(),
                                                                                2), 2),
                                        FontChinese)); //Delivery Qty // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
            else
                cell =
                    new Cell(new Phrase(currencyConverter.sformat(qrRound.roundQty(rectxstrSQL.QTY_RECV_STR_IN_COST_UNIT()),
                                                                  quantityDisplay),
                                        FontChinese)); //Delivery Qty // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        }
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        datatable.addCell(cell);

        //added by joan for GST enhancement 2014-12-15
        if (pohdrSQL.PO_CREATE_METHOD().equals("E") || pohdrSQL.PO_CREATE_METHOD().equals("R")) {
            discAmount = Double.parseDouble(qrMath.formatDecimal(postrallocSQL.GRP_DISC_AMT_STORE(), 2));
        } else {
            discAmount = Double.parseDouble(qrMath.formatDecimal(postrdetSQL.GRP_DISC_AMT_STORE(), 2));
        }
        //discAmount = postrdetSQL.GRP_DISC_AMT_STORE();
        cell =
            new Cell(new Phrase(currencyConverter.sformat(podetSQL.GRP_DISC_RATE(), percentageDisplay),
                                FontChinese)); //Discount // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        datatable.addCell(cell);

        //added by joan for GST enhancement 2014-12-15
        if (pohdrSQL.PO_CREATE_METHOD().equals("E") || pohdrSQL.PO_CREATE_METHOD().equals("R")) {
            if (itemmstSQL.ITEM_PERISH_CD().equals("Y") && itemmstSQL.ITEM_WEIGH_CD().equals("Y"))
                cell = new Cell(new Phrase(getDouble(qrMath.round(postrallocSQL.STORE_ITEM_SELL_WASTAGE(), 2), 2), FontChinese)); //Selling price
            else
                cell = new Cell(new Phrase("", FontChinese));
        } else {
            if (itemmstSQL.ITEM_PERISH_CD().equals("Y") && itemmstSQL.ITEM_WEIGH_CD().equals("Y"))
                cell = new Cell(new Phrase(currencyConverter.format(postrdetSQL.STORE_ITEM_SELL_WASTAGE()), FontChinese)); //Selling price // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
            else
                cell = new Cell(new Phrase("", FontChinese));
        }
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        datatable.addCell(cell);
        // Change by SALOW 2013-08-19 - For AEON ASEAN changes .
        //COST - Unit Price
        double cost_prc;
        // arvin - Redmine 8828 - 2014-05-07 for indent suppliers
        if (pohdrSQL.PO_ORD_SOURCE().equals("I")) {
            cost_prc = podetSQL.PO_COST() / pohdrSQL.PO_FRGN_CRNCY_EXCH_RATE();
        } else {
            cost_prc = podetSQL.PO_COST(); // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        }
        String strcost_prc =
            currencyConverter.format(cost_prc).toString(); // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        cell = new Cell(new Phrase(strcost_prc, FontChinese));
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        datatable.addCell(cell);

        // arvin - Redmine 8828 - 2014-05-07 for indent suppliers
        //COST - Amount
        double costAmt;
        //added by joan for GST enhancement
        if (pohdrSQL.PO_CREATE_METHOD().equals("E") || pohdrSQL.PO_CREATE_METHOD().equals("R")) {
            if (pohdrSQL.PO_ORD_SOURCE().equals("I")) {
                costAmt = postrallocSQL.EXT_COST_ORD_STORE() / pohdrSQL.PO_FRGN_CRNCY_EXCH_RATE();
                costamount = qrRound.roundCost(costAmt);
            } else {
                costamount =
                    qrRound.roundCost(postrallocSQL.EXT_COST_ORD_STORE()); // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
            }
        } else {
            if (pohdrSQL.PO_ORD_SOURCE().equals("I")) {
                //costAmt = podetSQL.EXT_COST_ORD() / pohdrSQL.PO_FRGN_CRNCY_EXCH_RATE();
                costAmt = postrdetSQL.EXT_COST_ORD_STORE() / pohdrSQL.PO_FRGN_CRNCY_EXCH_RATE();
                costamount = qrRound.roundCost(costAmt);
            } else {
                costamount =
                    qrRound.roundCost(postrdetSQL.EXT_COST_ORD_STORE()); // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
            }
        }
        String strext_cost_ord =
            currencyConverter.format(costamount).toString(); // Change by SALOW 2013-09-27 - For Currency Converter Implementation .

        cell = new Cell(new Phrase(strext_cost_ord, FontChinese));
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        datatable.addCell(cell);

        //RETAIL - Unit Price
        //added by joan for GST enhancement 2014-12-15
        double sell_prc = 0;
        if (strSYSShowPoRetailInfo != null &&
            strSYSShowPoRetailInfo.equals("N")) //added if else by whpuah 03/05/2017 -- display retail price only when SYSShowPoRetailInfo = Y
        {
            sell_prc = 0.00;
        } else {
            if (pohdrSQL.PO_CREATE_METHOD().equals("E") || pohdrSQL.PO_CREATE_METHOD().equals("R")) {
                sell_prc = Double.parseDouble(qrMath.formatDecimal(postrallocSQL.STORE_ITEM_SELL(), 2));
            } else {
                sell_prc = Double.parseDouble(qrMath.formatDecimal(postrdetSQL.STORE_ITEM_SELL(), 2));
            }
        }

        // postrdetSQL.STORE_ITEM_SELL(); // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        String strsell_prc =
            currencyConverter.format(sell_prc).toString(); // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        cell = new Cell(new Phrase(strsell_prc, FontChinese));
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        datatable.addCell(cell);

        //RETAIL - Amount
        //added by joan for GST enhancement 2014-12-15
        if (strSYSShowPoRetailInfo != null &&
            strSYSShowPoRetailInfo.equals("N")) //added if else by whpuah 03/05/2017 -- display retail price only when SYSShowPoRetailInfo = Y
        {
            retailamount = 0.00;
        } else {
            if (pohdrSQL.PO_CREATE_METHOD().equals("E") || pohdrSQL.PO_CREATE_METHOD().equals("R")) {
                retailamount = Double.parseDouble(qrMath.formatDecimal(postrallocSQL.EXT_SELL_ORD_STORE(), 2));
            } else {
                retailamount =
                    qrRound.roundSell(postrdetSQL.EXT_SELL_ORD_STORE()); // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
            }
        }
        String strext_sell_ord =
            currencyConverter.format(retailamount).toString(); // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        cell = new Cell(new Phrase(strext_sell_ord, FontChinese));
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        datatable.addCell(cell);

        // Code End .
        //Next Row
        if (strItem.length() > 34) //Item Desc
        {
            strItemDisp = strItem.substring(34);
            cell = new Cell(new Phrase(strItemDisp, FontChinese));
        } else {
            strItemDisp = "";
            cell = new Cell(new Phrase(strItemDisp, FontChinese));
        }
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.disableBorderSide(Rectangle.TOP);
        cell.enableBorderSide(Rectangle.BOTTOM);
        datatable.addCell(cell);

        if (itemmstSQL.SIZE_CD().length() > 0)
            cell = new Cell(new Phrase(itemmstSQL.SIZE_CD(), FontChinese)); //Size
        else
            cell = new Cell(new Phrase("\n", FontChinese)); //Size
        datatable.addCell(cell);

        if (itemmstSQL.COLOUR().length() > 0)
            cell = new Cell(new Phrase(itemmstSQL.COLOUR(), FontChinese)); //Color
        else
            cell = new Cell(new Phrase("\n", FontChinese)); //Color
        datatable.addCell(cell);

        if (itemmstSQL.SUBCLASS().length() > 0)
            cell = new Cell(new Phrase(itemmstSQL.STYLE(), FontChinese)); //Supplier Item No
        else
            cell = new Cell(new Phrase("\n", FontChinese)); //Supplier Item No
        datatable.addCell(cell);
        return datatable;
    }

    private Table createConsoPoDataTable(int prmCnt)
        //throws BadElementException, DocumentException ,SQLException
        throws Exception {
        // Change by SALOW 2013-08-19 - For AEON ASEAN changes .
        Table datatable = new Table(14);
        int headerwidths[] = { 3, 22, 7, 7, 9, 7, 5, 5, 6, 5, 6, 6, 6, 6 };
        // Code End .
        datatable.setPadding(0.5f);
        datatable.setSpacing(0);
        datatable.setWidths(headerwidths);
        datatable.setWidth(100);
        datatable.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
        datatable.setBorder(Rectangle.NO_BORDER);

        Cell cell = null;
        //cell = new Cell(new Phrase(qrMisc.leftFill(_cnt,2,"0"), FontChinese_no)); //No.
        cell = new Cell(new Phrase(String.valueOf(prmCnt), FontChinese_no)); //No.
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setRowspan(2);
        datatable.addCell(cell);

        String strItemDisp = "";
        //String strItem = getDescription(itemmstSQL.ITEM_DESC()); //Item Desc
        getItemmst(consoPodetSQL.SHORT_SKU());
        String strItem = getDescription(consoItemmstSQL.ITEM_DESC()); //Item Desc
        if (strItem.length() > 34) {
            strItemDisp = strItem.substring(0, 34);
            cell = new Cell(new Phrase(strItemDisp, FontChinese));
        } else {
            strItemDisp = strItem;
            cell = new Cell(new Phrase(strItemDisp, FontChinese));
        }
        cell.disableBorderSide(Rectangle.BOTTOM);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        datatable.addCell(cell);

        double dblQtyPerCase = 0;
        if (!consoPodetSQL.PO_COST_UNIT().equals(consoPodetSQL.PO_QTY_UNIT())) {
            unitcmstSQL.setFROM_UNIT(consoPodetSQL.PO_COST_UNIT());
            unitcmstSQL.setTO_UNIT(consoPodetSQL.PO_QTY_UNIT());
            if (unitcmstSQL.getByKey() > 0) {
                if (unitcmstSQL.CONV_FACTOR() >= 1)
                    dblQtyPerCase = unitcmstSQL.CONV_FACTOR();
                else {
                    unitcmstSQL.setFROM_UNIT(consoPodetSQL.PO_QTY_UNIT());
                    unitcmstSQL.setTO_UNIT(consoPodetSQL.PO_COST_UNIT());
                    unitcmstSQL.getByKey();
                    dblQtyPerCase = unitcmstSQL.CONV_FACTOR();
                }
            } else
                dblQtyPerCase = 0;
        } else if (consoPodetSQL.PO_COST_UNIT().equals(consoPodetSQL.PO_QTY_UNIT()))
            dblQtyPerCase = 1;

        if (itemmstSQL.ITEM_PERISH_CD().equals("Y"))
            cell = new Cell(new Phrase(currencyConverter.sformat(qrRound.round(dblQtyPerCase, 2), 2), FontChinese)); //QTY / CASE // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        else
            cell =
                new Cell(new Phrase(currencyConverter.sformat(qrRound.roundQty(dblQtyPerCase), quantityDisplay),
                                    FontChinese)); //QTY / CASE // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        datatable.addCell(cell);

        cell =
            new Cell(new Phrase(getDescription(getUnitDesc(consoPodetSQL.PO_COST_UNIT())),
                                FontChinese)); //UOM desc. ?itemmstSQL.RETAIL_UOM_DESC?
        datatable.addCell(cell);

        cell = new Cell(new Phrase(consoPodetSQL.SHORT_SKU(), FontChinese)); //ITEM NO
        datatable.addCell(cell);

        cell = new Cell(new Phrase(getBarCode(consoPodetSQL.SHORT_SKU()), FontChinese)); //Item Barcode
        cell.setRowspan(2);
        datatable.addCell(cell);

        //added by joan for GST enhancement 2014-12-16
        if (pohdrSQL.PO_CREATE_METHOD().equals("E") || pohdrSQL.PO_CREATE_METHOD().equals("R")) {
            if (itemmstSQL.ITEM_PERISH_CD().equals("Y"))
                cell = new Cell(new Phrase(getDouble(qrMath.round(consoPodetSQL.QTY_ORD(), 2), 2), FontChinese)); //Qty Order
            else
                cell = new Cell(new Phrase(getDouble(qrMath.round(consoPodetSQL.QTY_ORD(), 0), 0), FontChinese));
        } else {
            if (itemmstSQL.ITEM_PERISH_CD().equals("Y"))
                cell =
                    new Cell(new Phrase(currencyConverter.sformat(qrRound.round(consoPodetSQL.QTY_ORD() *
                                                                                consoPodetSQL.COST_UNIT_FACTOR(), 2),
                                                                  2),
                                        FontChinese)); //Qty Order // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
            else
                cell =
                    new Cell(new Phrase(currencyConverter.sformat(qrRound.roundQty(consoPodetSQL.QTY_ORD() *
                                                                                   consoPodetSQL.COST_UNIT_FACTOR()),
                                                                  quantityDisplay),
                                        FontChinese)); // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        }
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        datatable.addCell(cell);

        String store = "";
        if (pohdrSQL.DELIVER_TO_DC().equals("Y")) {
            store = pohdrSQL.DELV_DC_CODE();
        } else if (pohdrSQL.DELV_TO_STORE() != null && pohdrSQL.DELV_TO_STORE().length() > 0) {
            store = pohdrSQL.DELV_TO_STORE();
        }
        getDelvQtyRecdet(store, pohdrSQL.PO_RECPT(), pohdrSQL.COY(), pohdrSQL.ORD_NO(), consoPodetSQL.SHORT_SKU(),
                         String.valueOf(consoPodetSQL.PO_SEQ()));

        if (itemmstSQL.ITEM_PERISH_CD().equals("Y"))
            cell =
                new Cell(new Phrase(currencyConverter.sformat(qrRound.round(recdetSQL.QTY_RECV_IN_COST_UNIT(), 2), 2),
                                    FontChinese)); //Delivery Qty // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        else
            cell =
                new Cell(new Phrase(currencyConverter.sformat(qrRound.roundQty(recdetSQL.QTY_RECV_IN_COST_UNIT()),
                                                              quantityDisplay),
                                    FontChinese)); //Delivery Qty // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        datatable.addCell(cell);

        discAmount = consoPodetSQL.GRP_DISC_AMT();
        cell =
            new Cell(new Phrase(currencyConverter.sformat(qrRound.roundDisc(consoPodetSQL.GRP_DISC_RATE()),
                                                          percentageDisplay),
                                FontChinese)); //Discount // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        datatable.addCell(cell);

        if (itemmstSQL.ITEM_PERISH_CD().equals("Y") && itemmstSQL.ITEM_WEIGH_CD().equals("Y"))
            cell = new Cell(new Phrase(currencyConverter.format(consoPodetSQL.PO_SELL_WASTAGE()), FontChinese)); //Selling price // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        else
            cell = new Cell(new Phrase("", FontChinese));
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        datatable.addCell(cell);
        // Change by SALOW 2013-08-19 - For AEON ASEAN changes .
        //COST - Unit Price
        double cost_prc =
            consoPodetSQL.PO_COST(); // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        String strcost_prc =
            currencyConverter.format(cost_prc).toString(); // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        cell = new Cell(new Phrase(strcost_prc, FontChinese));
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        datatable.addCell(cell);

        //COST - Amount
        costamount =
            qrRound.roundCost(consoPodetSQL.EXT_COST_ORD()); // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        String strext_cost_ord =
            currencyConverter.format(costamount).toString(); // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        cell = new Cell(new Phrase(strext_cost_ord, FontChinese));
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        datatable.addCell(cell);

        //RETAIL - Unit Price
        double sell_prc =
            consoPodetSQL.PO_SELL(); // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        String strsell_prc = "";
        if (strSYSShowPoRetailInfo != null &&
            strSYSShowPoRetailInfo.equals("N")) //added if else by whpuah 03/05/2017 -- display retail price only when SYSShowPoRetailInfo = Y
        {
            strsell_prc = currencyConverter.format(0).toString();
        } else {
            strsell_prc =
                currencyConverter.format(sell_prc).toString(); // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        }
        cell = new Cell(new Phrase(strsell_prc, FontChinese));
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        datatable.addCell(cell);

        //RETAIL - Amount
        retailamount =
            qrRound.roundSell(consoPodetSQL.EXT_SELL_ORD()); // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        String strext_sell_ord = "";
        if (strSYSShowPoRetailInfo != null &&
            strSYSShowPoRetailInfo.equals("N")) //added if else by whpuah 03/05/2017 -- display retail price only when SYSShowPoRetailInfo = Y
        {
            strext_sell_ord = currencyConverter.format(0).toString();
        } else {
            strext_sell_ord =
                currencyConverter.format(retailamount).toString(); // Change by SALOW 2013-09-27 - For Currency Converter Implementation .
        }
        cell = new Cell(new Phrase(strext_sell_ord, FontChinese));
        cell.setRowspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        datatable.addCell(cell);
        // Code End .

        //Next Row
        if (strItem.length() > 34) //Item Desc
        {
            strItemDisp = strItem.substring(34);
            cell = new Cell(new Phrase(strItemDisp, FontChinese));
        } else {
            strItemDisp = "";
            cell = new Cell(new Phrase(strItemDisp, FontChinese));
        }
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.disableBorderSide(Rectangle.TOP);
        cell.enableBorderSide(Rectangle.BOTTOM);
        datatable.addCell(cell);

        if (itemmstSQL.SIZE_CD().length() > 0)
            //cell = new Cell(new Phrase(itemmstSQL.SIZE_CD(),FontChinese)); //Size
            cell = new Cell(new Phrase(consoItemmstSQL.SIZE_CD(), FontChinese)); //Size
        else
            cell = new Cell(new Phrase("\n", FontChinese)); //Size
        datatable.addCell(cell);

        if (itemmstSQL.COLOUR().length() > 0)
            //cell = new Cell(new Phrase(itemmstSQL.COLOUR(),FontChinese)); //Color
            cell = new Cell(new Phrase(consoItemmstSQL.COLOUR(), FontChinese)); //Color
        else
            cell = new Cell(new Phrase("\n", FontChinese)); //Color
        datatable.addCell(cell);

        if (itemmstSQL.SUBCLASS().length() > 0)
            //cell = new Cell(new Phrase(itemmstSQL.STYLE(),FontChinese)); //Supplier Item No
            cell = new Cell(new Phrase(consoItemmstSQL.STYLE(), FontChinese)); //Supplier Item No
        else
            cell = new Cell(new Phrase("\n", FontChinese)); //Supplier Item No
        datatable.addCell(cell);
        return datatable;
    }

    private Table createTableFooter() throws BadElementException, DocumentException, SQLException {
        Table tableFooter = new Table(9);
        int headerwidths[] = { 12, 25, 13, 1, 3, 3, 3, 5, 34 };

        tableFooter.setPadding(1);
        tableFooter.setSpacing(0);
        tableFooter.setWidths(headerwidths);
        tableFooter.setWidth(100);
        tableFooter.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
        tableFooter.setBorder(Rectangle.NO_BORDER);

        Cell cell = null;

        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_SUPPLIER_NO"), FontChinese));
        tableFooter.addCell(cell);
        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_SUPPLIER_NAME"), FontChinese));
        tableFooter.addCell(cell);
        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_CONTRACT"), FontChinese));
        tableFooter.addCell(cell);
        cell = new Cell(new Phrase(getTranslatedReportMsg(""), FontChinese));
        cell.disableBorderSide(Rectangle.BOX);
        tableFooter.addCell(cell);
        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_DATE_RECEIVED"), FontChinese));
        cell.setColspan(3);
        tableFooter.addCell(cell);
        cell = new Cell(new Phrase(getTranslatedReportMsg(""), FontChinese));
        cell.disableBorderSide(Rectangle.BOX);
        tableFooter.addCell(cell);
        cell = new Cell(new Phrase(getTranslatedReportMsg("SLIP_REMARK"), FontChinese));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.enableBorderSide(Rectangle.BOX);
        cell.disableBorderSide(Rectangle.BOTTOM);
        tableFooter.addCell(cell);

        cell = new Cell(new Phrase(vnmstrSQL.VN_CODE(), FontChinese)); //Supplier No
        tableFooter.addCell(cell);

        cell = new Cell(new Phrase(getDescription(vnmstrSQL.VN_NAME()), FontChinese)); //Supplier Name
        tableFooter.addCell(cell);

        cell = new Cell(new Phrase(pohdrSQL.PO_SUPPL_CONTRACT(), FontChinese)); //Contract No
        tableFooter.addCell(cell);

        cell = new Cell(new Phrase(getTranslatedReportMsg(""), FontChinese));
        cell.disableBorderSide(Rectangle.BOX);
        tableFooter.addCell(cell);

        //Date Received
        cell = new Cell(new Phrase(strDateRecv.length() > 0 ? strDateRecv.substring(0, 4) : "", FontChinese));
        tableFooter.addCell(cell);
        cell = new Cell(new Phrase(strDateRecv.length() > 0 ? strDateRecv.substring(5, 7) : "", FontChinese));
        tableFooter.addCell(cell);
        cell = new Cell(new Phrase(strDateRecv.length() > 0 ? strDateRecv.substring(8, 10) : "", FontChinese));
        tableFooter.addCell(cell);
        cell = new Cell(new Phrase(getTranslatedReportMsg(""), FontChinese));
        cell.disableBorderSide(Rectangle.BOX);
        tableFooter.addCell(cell);

        //added by joan for GST enhancement
        if (pohdrSQL.PO_CREATE_METHOD().equals("E") || pohdrSQL.PO_CREATE_METHOD().equals("R")) {
            cell =
                new Cell(new Phrase(getTranslatedReportMsg("SLIP_SORTING_QUANTITY_BY_RETAIL_UOM"),
                                    FontChinese)); //Remark
        } else {
            if (blnIsPreDis) //true
            {
                cell =
                    new Cell(new Phrase(pohdrSQL.PO_CREATE_METHOD().equals("D") ?
                                        getTranslatedReportMsg("SLIP_CDO_NO") + ": " + pohdrSQL.PO_REF_NO() : "\n\n",
                                        FontChinese)); //Remark
            } else //false
            {
                //Added by Genesis 2017-06-13 SRAM019406
                classmstSQL.setCLASS(pohdrSQL.PO_GROUP());
                if (classmstSQL.getByKey() > 0) {
                    if (classmstSQL.REMARK_PO() != null && classmstSQL.REMARK_PO().equals("Y")) {
                        cell =
                            new Cell(new Phrase(getTranslatedReportMsg(pohdrSQL.REMARK()), FontChinese)); //Remark
                    } else {
                        cell = new Cell(new Phrase("", FontChinese)); //Remark
                    }
                }
                //Done Added by Genesis 2017-06-13 SRAM019406
            }
        }
        //cell = new Cell(new Phrase(pohdrSQL.PO_CREATE_METHOD().equals("D") ? getTranslatedReportMsg("SLIP_CDO_NO")+": "+pohdrSQL.PO_REF_NO() : "\n\n", FontChinese)); //Remarked
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.enableBorderSide(Rectangle.LEFT);
        cell.enableBorderSide(Rectangle.RIGHT);
        tableFooter.addCell(cell);

        cell = new Cell(new Phrase(getTranslatedReportMsg(""), FontChinese));
        cell.setColspan(8);
        cell.disableBorderSide(Rectangle.BOX);
        tableFooter.addCell(cell);
        cell = new Cell(new Phrase(getTranslatedReportMsg("\n"), FontChinese)); //Remark
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.enableBorderSide(Rectangle.BOX);
        cell.disableBorderSide(Rectangle.TOP);
        tableFooter.addCell(cell);

        return tableFooter;
    }

    private void getPmgrp(String _class) throws SQLException {
        ResultSet rs_pmgrp = null;

        if (psPmgrp == null) {
            String pmgrp_query =
                " SELECT DIV_PMGRP " + " FROM   CLASSMST, DEPTMST, DIVMST " + " WHERE  CLASSMST.CLASS = ? " +
                " AND    CLASSMST.CLASS_DEPT = DEPTMST.DEPT " + " AND    DEPTMST.DEPT_DIV = DIVMST.DIV ";
            psPmgrp = conn.prepareStatement(pmgrp_query);
        }
        psPmgrp.setString(1, _class);
        rs_pmgrp = psPmgrp.executeQuery();

        if (rs_pmgrp != null && rs_pmgrp.next())
            strLineCode = rs_pmgrp.getString(1);

        if (rs_pmgrp != null) {
            rs_pmgrp.close();
            rs_pmgrp = null;
        }
    }

    /*private long getDelvQtyRecdet()
    throws SQLException
  {
    ResultSet rsDelvQty = null;

    if (psRecdet == null) {
      psRecdet = conn.prepareStatement("SELECT rd.QTY_RECV_IN_COST_UNIT " +
                                       "FROM POHDR ph, PODET pd, RECHDR rh, RECDET rd " +
                                       "WHERE ph.COY = pd.COY " +
                                       "AND ph.ORD_NO = pd.ORD_NO " +
                                       "AND rh.RECPT_STORE = rd.RECPT_STORE " +
                                       "AND rh.RECPT_NO = rd.RECPT_NO " +
                                       "AND pd.SHORT_SKU = rd.SHORT_SKU " +
                                       "AND ph.COY = rh.COY " +
                                       "AND ph.ORD_NO = rh.DOCUMENT_NO " +
                                       "AND ph.COY = ? " +
                                       "AND ph.ORD_NO = ? " +
                                       "AND pd.SHORT_SKU = ?");
    }
    psRecdet.setString(1, pohdrSQL.COY());
    psRecdet.setString(2, pohdrSQL.ORD_NO());
    psRecdet.setString(3, podetSQL.SHORT_SKU());
    rsDelvQty = psRecdet.executeQuery();

    if(rsDelvQty != null && rsDelvQty.next())
      lngDelvQty = rsDelvQty.getLong(1);
    else
      lngDelvQty = 0;

    if(rsDelvQty != null) {
      rsDelvQty.close();
      rsDelvQty = null;
    }
    return lngDelvQty;
  }*/

    private void getDelvQtyRecdet(String _store, String _recpt_no, String _coy, String _ordNo, String _sku,
                                  String _poSeq) throws Exception {
        recdetSQL.setRECPT_STORE(_store);
        recdetSQL.setRECPT_NO(_recpt_no);
        recdetSQL.setCOY(_coy);
        recdetSQL.setORD_NO(_ordNo);
        recdetSQL.setSHORT_SKU(_sku);
        recdetSQL.setRECPT_SEQ_NO(Integer.parseInt(_poSeq));

        if (recdetSQL.getByKey() == 0)
            recdetSQL.setVObject(new RecdetInfo().getVObject());
    }

    /*private long getDelvQtyRectxstr()
    throws SQLException
  {
    ResultSet rsDelvQtyStr = null;

    if (psRectxstr == null) {
      psRectxstr = conn.prepareStatement("SELECT rd.QTY_RECV_STR_IN_COST_UNIT " +
                                       "FROM POHDR ph, POSTRDET pd, RECHDR rh, RECTXSTR rd " +
                                       "WHERE ph.COY = pd.COY " +
                                       "AND ph.ORD_NO = pd.ORD_NO " +
                                       "AND rh.RECPT_STORE = rd.RECPT_STORE " +
                                       "AND rh.RECPT_NO = rd.RECPT_NO " +
                                       "AND pd.SHORT_SKU = rd.SHORT_SKU " +
                                       "AND pd.STORE = rd.STORE " +
                                       "AND ph.COY = rh.COY " +
                                       "AND ph.ORD_NO = rh.DOCUMENT_NO " +
                                       "AND ph.COY = ? " +
                                       "AND ph.ORD_NO = ? " +
                                       "AND pd.STORE = ? " +
                                       "AND pd.SHORT_SKU = ?");
    }
    psRectxstr.setString(1, pohdrSQL.COY());
    psRectxstr.setString(2, pohdrSQL.ORD_NO());
    psRectxstr.setString(3, postrdetSQL.STORE());
    psRectxstr.setString(4, postrdetSQL.SHORT_SKU());
    rsDelvQtyStr = psRectxstr.executeQuery();

    if(rsDelvQtyStr != null && rsDelvQtyStr.next())
      lngDelvQty = rsDelvQtyStr.getLong(1);
    else
      lngDelvQty = 0;
    if(rsDelvQtyStr != null) {
      rsDelvQtyStr.close();
      rsDelvQtyStr = null;
    }
    return lngDelvQty;
  }*/

    private void getDelvQtyRectxstr(String _recptStore, String _recpt_no, String _coy, String _ordNo, String _sku,
                                    String _poSeq, String _store) throws Exception {
        rectxstrSQL.setRECPT_STORE(_recptStore);
        rectxstrSQL.setRECPT_NO(_recpt_no);
        rectxstrSQL.setCOY(_coy);
        rectxstrSQL.setORD_NO(_ordNo);
        rectxstrSQL.setSHORT_SKU(_sku);
        rectxstrSQL.setRECPT_SEQ_NO(Integer.parseInt(_poSeq));
        rectxstrSQL.setSTORE(_store);

        if (rectxstrSQL.getByKey() == 0)
            rectxstrSQL.setVObject(new RectxstrInfo().getVObject());
    }

    // duped by guoliang from getDelvQtyRectxstr for recstralloc
    private void getDelvQtyRecstralloc(String _recptStore, String _recpt_no, String _coy, String _ordNo, String _sku,
                                       String _poSeq, String _store) throws Exception {
        recstrallocSQL.setRECPT_STORE(_recptStore);
        recstrallocSQL.setRECPT_NO(_recpt_no);
        recstrallocSQL.setCOY(_coy);
        recstrallocSQL.setORD_NO(_ordNo);
        recstrallocSQL.setSHORT_SKU(_sku);
        recstrallocSQL.setRECPT_SEQ_NO(Integer.parseInt(_poSeq));
        recstrallocSQL.setSTORE(_store);

        if (recstrallocSQL.getByKey() == 0)
            recstrallocSQL.setVObject(new RectxstrInfo().getVObject());
    }

    private String getBarCode(String prmSku) throws SQLException {
        String barcode = "";
        ResultSet rs_barcode = null;
        if (psBarcode == null) {
            String barcode_query =
                "SELECT BARCODE " + "FROM   XBARCODE " + "WHERE  BARCD_PRIMARY = 'Y' " + "AND    SHORT_SKU = ? ";
            psBarcode = conn.prepareStatement(barcode_query);
        }
        psBarcode.setString(1, prmSku);
        rs_barcode = psBarcode.executeQuery();

        while (rs_barcode != null && rs_barcode.next())
            barcode = rs_barcode.getString(1);

        if (rs_barcode != null) {
            rs_barcode.close();
            rs_barcode = null;
        }
        return barcode;
    }

    private String getUnitDesc(String unit) throws SQLException {
        unitmstSQL.setUNIT(unit);
        if (unitmstSQL.getByKey() == 0)
            unitmstSQL.setUNIT_DESC("");

        return unitmstSQL.UNIT_DESC();
    }

    private void getStrmst(String strStore) throws SQLException {
        strmstSQL.setSTORE(strStore);
        if (strmstSQL.getByKey() == 0)
            strmstSQL.setVObject(new StrmstInfo().getVObject());
    }

    private void getVnmstr(String prmVnCode) throws SQLException {
        vnmstrSQL.setVN_CODE(prmVnCode);
        if (vnmstrSQL.getByKey() == 0)
            vnmstrSQL.setVObject(new VnmstrInfo().getVObject());
    }

    private void getVnbranch(String prmVnCode, String prmBranchCode) throws SQLException {
        vnbranchSQL.setVN_CODE(prmVnCode);
        vnbranchSQL.setBRANCH_CODE(prmBranchCode);

        if (vnbranchSQL.getByKey() == 0)
            vnbranchSQL.setVObject(new VnbranchInfo().getVObject());
    }

    private void getItemmst(String sku) throws SQLException {
        consoItemmstSQL.setSHORT_SKU(sku);
        if (consoItemmstSQL.getByKey() == 0)
            consoItemmstSQL.setVObject(new ItemmstInfo().getVObject());
    }

    private java.sql.Date getDateRecv(String prmCoy, String prmOrdNo) throws SQLException {
        Date dtRecDate = null;
        ResultSet rsDateRecv = null;
        if (psDateRecv == null) {
            String strQryDateRecv =
                "SELECT DATE_RECV FROM RECHDR WHERE COY = ? AND DOCUMENT_NO = ? AND RECPT_TYPE = 'P'";
            psDateRecv = conn.prepareStatement(strQryDateRecv);
        }
        psDateRecv.setString(1, prmCoy);
        psDateRecv.setString(2, prmOrdNo);
        rsDateRecv = psDateRecv.executeQuery();

        while (rsDateRecv != null && rsDateRecv.next())
            dtRecDate = rsDateRecv.getDate(1);
        if (rsDateRecv != null) {
            rsDateRecv.close();
            rsDateRecv = null;
        }
        return dtRecDate;
    }

    private String getProfitvv(String strVnm) throws SQLException {
        String strVNM_VDTVL = "";

        profitvvSQL.setVNM(strVnm);
        profitvvSQL.setCOY(strCOY);
        if (!(profitvvSQL.getByKey() == 0)) {
            strVNM_VDTVL = getDescription(profitvvSQL.VNM_VDTVL());
        }
        return strVNM_VDTVL;
    }

    private String getAduser(String usr_id) throws SQLException {
        String usr_first_name = "";
        aduserSQL.setUSR_ID(usr_id);
        if (aduserSQL.getByKey() == 0)
            usr_first_name = "";
        else
            usr_first_name = getDescription(aduserSQL.USR_FIRST_NAME());

        return (usr_first_name);
    }

    private void getSlipProperty() throws SQLException {
        strToParty = "";
        //added by joan for GST enhancement 2014-12-15
        if (pohdrSQL.PO_CREATE_METHOD().equals("E") || pohdrSQL.PO_CREATE_METHOD().equals("R")) {
            //if(pohdrSQL.CONSOLIDATE_FLAG().equals("Y")) {
            if (pohdrSQL.CONSOLIDATE_FLAG() != null && !pohdrSQL.CONSOLIDATE_FLAG().equals("N")) {
                if (blnIsPreDis) {
                    strRptTitle = getTranslatedReportMsg("SLIP_STORE_ALLOCATION_PURCHASE_ORDER_RETAIL_UOM");
                    strToParty = "";
                } else {
                    strRptTitle = getTranslatedReportMsg("SLIP_PURCHASE_ORDER_RETAIL_UOM");
                    strToParty = getTranslatedReportMsg("SLIP_MERCHANDISING_SUPPLIER");
                }
            }
            //else if(pohdrSQL.CONSOLIDATE_FLAG().equals("N")) {
        } else {
            //if(pohdrSQL.CONSOLIDATE_FLAG().equals("Y")) {
            if (pohdrSQL.CONSOLIDATE_FLAG() != null && !pohdrSQL.CONSOLIDATE_FLAG().equals("N")) {
                if (blnIsPreDis) {
                    strRptTitle = getTranslatedReportMsg("SLIP_PRE_DISTRIBUTE_PURCHASE_ORDER");
                    strToParty = "";
                } else {
                    strRptTitle = getTranslatedReportMsg("SLIP_CONSOLIDATE_PURCHASE_ORDER");
                    strToParty = getTranslatedReportMsg("SLIP_MERCHANDISING_SUPPLIER");
                }
            }
            //else if(pohdrSQL.CONSOLIDATE_FLAG().equals("N")) {
            else {
                strRptTitle = getTranslatedReportMsg("SLIP_PURCHASE_ORDER");
                strToParty = getTranslatedReportMsg("SLIP_MERCHANDISING_LINE_SUPPLIER");
            }
        }
    }

    public void getCOYSUBMST(String coy, String coy_sub) throws Exception {
        coysubmstSQL.setCOY(coy);
        coysubmstSQL.setCOY_SUB(coy_sub);
        coysubmstSQL.getByKey();
    }

    public void getVNCNTERM(String strVN_CODE, String strVN_CONTRACT) throws Exception {
        vncntermSQL.setVN_CODE(strVN_CODE);
        vncntermSQL.setVN_CONTRACT(strVN_CONTRACT);
        vncntermSQL.getByKey();
    }

    public void getDecimalDisplay(String strCOY) throws Exception {
        profitvvSQL.setCOY(strCOY);
        profitvvSQL.setVNM("SYSDecimalDisplay");
        profitvvSQL.getByKey();

        decimalDisplay = Integer.parseInt(profitvvSQL.VNM_VDTVL());
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

    // for local testing only
    // public void setTestRun(boolean bln)
    // {
    //     test_run = bln;
    // }

    // protected void openConnection() throws SQLException
    // {
    //    if (test_run)
    //     {
    //        java.sql.DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
    //        conn = java.sql.DriverManager.getConnection("jdbc:oracle:thin:@188.188.8.9:1521:aeonmal" , "profit", "profit1888");
    //     }
    //     else
    //        super.openConnection();
    //  }

    public static void main(String[] args) {
        try {

            HParam hParam = new HParam();

            hParam.put("User_ID", "OPR");
            hParam.put("SESSION_ID", "ac2d068a22b8c0a3d3a504554b2ca395a121e8d5181e");
            hParam.put("TIME_CD", "1415097179596");
            hParam.put("RPTPARAM", "Y");
            hParam.put("ACTION", "Print_PO");
            hParam.put("COY", "AMY");
            //
            //hParam.put("User_ID","OPR");
            // hParam.put("SESSION_ID","ac2d068a22b8fb7486aad7d246a6b624f9f5e98a050b");
            // hParam.put("TIME_CD","1415097813411");
            // hParam.put("RPTPARAM","Y");
            // hParam.put("ACTION","Print_PO");
            // hParam.put("COY","AMY");

            PrintPurchaseOrderSlip_Type3 instance =
                new PrintPurchaseOrderSlip_Type3("/tmp/PrintPurchaseOrderSlip_Type3" + System.currentTimeMillis() +
                                                 ".pdf");
            // instance.setTestRun(true);
            instance.print(hParam);
        } catch (Exception de) {
            de.printStackTrace();
        }
        System.exit(0);
    }

    private static final boolean IS_TEST_RUN = false;
}
