// Decompiled by DJ v3.5.5.77 Copyright 2003 Atanas Neshkov  Date: 9/9/2015 2:41:45 PM
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   ConsSalesStmtRpt_2.java

package qrcom.PROFIT.reports.CC;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat; 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.Region;
import qrcom.PROFIT.files.info.*;
import qrcom.PROFIT.reports.GenericExcel;
import qrcom.util.*;
import qrcom.util.ejb.jdbc.support.Parameters;
import qrcom.util.ejb.jdbc.support.StatementManager;

// Referenced classes of package qrcom.PROFIT.reports.CC:
//            ConsSalesStmtRptQueryBuilder_2

public class ConsSalesStmtRpt_2 extends GenericExcel
{

//    private static final Log log = LogFactory.getLog(ConsSalesStmtRpt_2.class);
    private static final String PROGRAM_VERSION = "2015-03-13 00:00 cltan";
    final short borderTHIN = 1;
    private String USER_ID;
    private String COY_SUB;
    private String SUPPL;
    private String SUPPL_CONT;
    private String FR_CONCESS_DATE;
    private String TO_CONCESS_DATE;
    private String SALES_PERIOD;
    private String LOGO_NAME;
    private String LOGO_ADDR1;
    private String LOGO_ADDR2;
    private String LOGO_TEL_FAX;
    private String LOGO_GST_REG_NO;
    private String strDateFormat;
    private ConsSalesStmtRptQueryBuilder_2 concessQueryBuilder;
    private HSSFSheet sheet;
    private HSSFWorkbook workBook;
    private HSSFRow headerRow;
    private HSSFRow headerRowSub;
    private HSSFCell headerCell;
    private HSSFCellStyle superheaderstyle;
    private HSSFCellStyle centerheaderstyle;
    private HSSFCellStyle headerstyle;
    private HSSFCellStyle cellstyle;
    private HSSFCellStyle cellAmountstyle;
    private HSSFCellStyle cellTtlAmountstyle;
    private HSSFCellStyle cellNumberstyle;
    private HSSFCellStyle cellMarginstyle;
    private HSSFCellStyle cellTotalstyle;
    private HSSFCellStyle cellGrdTtlstyle;
    private Region region;
    private SimpleDateFormat dateFormatter;
    private PreparedStatement pstmt;
    private PreparedStatement psCmhdr;
    private PreparedStatement psHdr;
    private ResultSet resultSet;
    private ResultSet rsComm;
    private ResultSet rsHdr;
    private ProfitvvSQL profitvvSQL;
    private CoymstSQL coymstSQL;
    private CoysubmstSQL coysubmstSQL;
    private StrmstSQL strmstSQL;
    private ClassmstSQL classmstSQL;
    private ItemmstSQL itemmstSQL;
    private VnmstrSQL vnmstrSQL;
    private PohdrSQL pohdrSQL;
    private CurrencyConverter webCurrConverter;
    private int start_row;
    private int begin_row;
    private int temp_row;
    private String SYSCompanyLogo;
    private String SYSReportLine;
    private String SYSDefaultLanguage;
    private String SYSMaxLinesPerSheet;
    private String SYSB2BPoAdjConcess;
    private String SYSCurrCom;
    private String SYSGenInvNo; //added by Chiew 2017-02-19
    private String SYSVRInvFormula; //added by Chiew 2017-02-19
    private String SYSCtrlGRNInv; //added by Chiew 2017-02-19
    private String SYSPrintConSlsStmt; //added by Chiew 2017-02-19
    private int maxPerSheet;
    private int currentRow;
    private int noSheet;
    private int countArray;
    private int intTitleShort;
    private int intTotalColumn;
    private int intTtlByPlace;
    private int intLogoSize;
    private int intTitleSize;
    private int intHeaderSize;
    private int intTableSize;
    private int intLineMover;
    private int intLogoMover;
    private int intTitleMover;
    private int intHeaderMover;
    private int intTableMover;
    private int intDataMover;
    private double dblTotSellWovat;
    private double dblTotSellVat;
    private double dblTotCostAmount;
    private double dblTotCommWovat;
    private double dblTotCommVat;
    private double dblTotCommWvat;
    private double dblTotCostWovat;
    private double dblTotCostVat;
    private double dblTotCostWvat;
    private double dblGrdTotSellWovat;
    private double dblGrdTotSellVat;
    private double dblGrdTotCostAmount;
    private double dblGrdTotCommWovat;
    private double dblGrdTotCommVat;
    private double dblGrdTotCommWvat;
    private double dblGrdTotCostWovat;
    private double dblGrdTotCostVat;
    private double dblGrdTotCostWvat;

    private TaxrptctrlSQL taxrptctrlSQL = null;
    private String taxRptFormat = "1";
    private static final String DOC_TYPE = "Concessionaire Sales Statement";

    public ConsSalesStmtRpt_2()
    {
        USER_ID = null;
        COY_SUB = null;
        SUPPL = null;
        SUPPL_CONT = null;
        FR_CONCESS_DATE = null;
        TO_CONCESS_DATE = null;
        SALES_PERIOD = null;
        LOGO_NAME = "";
        LOGO_ADDR1 = "";
        LOGO_ADDR2 = "";
        LOGO_TEL_FAX = "";
        LOGO_GST_REG_NO = "";
        strDateFormat = "dd/MM/yyyy";
        concessQueryBuilder = null;
        sheet = null;
        workBook = null;
        headerRow = null;
        headerRowSub = null;
        headerCell = null;
        superheaderstyle = null;
        centerheaderstyle = null;
        headerstyle = null;
        cellstyle = null;
        cellAmountstyle = null;
        cellTtlAmountstyle = null;
        cellNumberstyle = null;
        cellMarginstyle = null;
        cellTotalstyle = null;
        cellGrdTtlstyle = null;
        region = null;
        dateFormatter = null;
        pstmt = null;
        psCmhdr = null;
        psHdr = null;
        resultSet = null;
        rsComm = null;
        rsHdr = null;
        profitvvSQL = null;
        coymstSQL = null;
        coysubmstSQL = null;
        strmstSQL = null;
        classmstSQL = null;
        itemmstSQL = null;
        vnmstrSQL = null;
        pohdrSQL = null;
        start_row = 0;
        begin_row = 0;
        temp_row = 0;
        SYSCompanyLogo = "";
        SYSReportLine = "";
        SYSDefaultLanguage = "";
        SYSMaxLinesPerSheet = "";
        SYSB2BPoAdjConcess = "";
        SYSCurrCom = "";
        SYSGenInvNo = ""; //added by Chiew 2017-02-19
        SYSVRInvFormula = ""; //added by Chiew 2017-02-19
        SYSCtrlGRNInv = ""; //added by Chiew 2017-02-19        
        maxPerSheet = 0;
        currentRow = 0;
        noSheet = 2;
        countArray = 0;
        intTitleShort = 300;
        intTotalColumn = 22 + 14;
        intTtlByPlace = 6;
        intLogoSize = 7;
        intTitleSize = 1;
        intHeaderSize = 7;
        intTableSize = 2;
        intLineMover = 0;
        intLogoMover = 0;
        intTitleMover = 0;
        intHeaderMover = 0;
        intTableMover = 0;
        intDataMover = 0;
        dblTotSellWovat = 0.0D;
        dblTotSellVat = 0.0D;
        dblTotCostAmount = 0.0D;
        dblTotCommWovat = 0.0D;
        dblTotCommVat = 0.0D;
        dblTotCommWvat = 0.0D;
        dblTotCostWovat = 0.0D;
        dblTotCostVat = 0.0D;
        dblTotCostWvat = 0.0D;
        dblGrdTotSellWovat = 0.0D;
        dblGrdTotSellVat = 0.0D;
        dblGrdTotCostAmount = 0.0D;
        dblGrdTotCommWovat = 0.0D;
        dblGrdTotCommVat = 0.0D;
        dblGrdTotCommWvat = 0.0D;
        dblGrdTotCostWovat = 0.0D;
        dblGrdTotCostVat = 0.0D;
        dblGrdTotCostWvat = 0.0D;
    }

    public ConsSalesStmtRpt_2(String file_name)
    {
        super(file_name);
        USER_ID = null;
        COY_SUB = null;
        SUPPL = null;
        SUPPL_CONT = null;
        FR_CONCESS_DATE = null;
        TO_CONCESS_DATE = null;
        SALES_PERIOD = null;
        LOGO_NAME = "";
        LOGO_ADDR1 = "";
        LOGO_ADDR2 = "";
        LOGO_TEL_FAX = "";
        LOGO_GST_REG_NO = "";
        strDateFormat = "dd/MM/yyyy";
        concessQueryBuilder = null;
        sheet = null;
        workBook = null;
        headerRow = null;
        headerRowSub = null;
        headerCell = null;
        superheaderstyle = null;
        centerheaderstyle = null;
        headerstyle = null;
        cellstyle = null;
        cellAmountstyle = null;
        cellTtlAmountstyle = null;
        cellNumberstyle = null;
        cellMarginstyle = null;
        cellTotalstyle = null;
        cellGrdTtlstyle = null;
        region = null;
        dateFormatter = null;
        pstmt = null;
        psCmhdr = null;
        psHdr = null;
        resultSet = null;
        rsComm = null;
        rsHdr = null;
        profitvvSQL = null;
        coymstSQL = null;
        coysubmstSQL = null;
        strmstSQL = null;
        classmstSQL = null;
        itemmstSQL = null;
        vnmstrSQL = null;
        pohdrSQL = null;
        start_row = 0;
        begin_row = 0;
        temp_row = 0;
        SYSCompanyLogo = "";
        SYSReportLine = "";
        SYSDefaultLanguage = "";
        SYSMaxLinesPerSheet = "";
        SYSB2BPoAdjConcess = "";
        SYSCurrCom = "";
        SYSGenInvNo = ""; //added by Chiew 2017-02-19
        SYSVRInvFormula = ""; //added by Chiew 2017-02-19
        SYSCtrlGRNInv = ""; //added by Chiew 2017-02-19      
        maxPerSheet = 0;
        currentRow = 0;
        noSheet = 2;
        countArray = 0;
        intTitleShort = 300;
        intTotalColumn = 22 + 14;
        intTtlByPlace = 6;
        intLogoSize = 7;
        intTitleSize = 1;
        intHeaderSize = 7;
        intTableSize = 2;
        intLineMover = 0;
        intLogoMover = 0;
        intTitleMover = 0;
        intHeaderMover = 0;
        intTableMover = 0;
        intDataMover = 0;
        dblTotSellWovat = 0.0D;
        dblTotSellVat = 0.0D;
        dblTotCostAmount = 0.0D;
        dblTotCommWovat = 0.0D;
        dblTotCommVat = 0.0D;
        dblTotCommWvat = 0.0D;
        dblTotCostWovat = 0.0D;
        dblTotCostVat = 0.0D;
        dblTotCostWvat = 0.0D;
        dblGrdTotSellWovat = 0.0D;
        dblGrdTotSellVat = 0.0D;
        dblGrdTotCostAmount = 0.0D;
        dblGrdTotCommWovat = 0.0D;
        dblGrdTotCommVat = 0.0D;
        dblGrdTotCommWvat = 0.0D;
        dblGrdTotCostWovat = 0.0D;
        dblGrdTotCostVat = 0.0D;
        dblGrdTotCostWvat = 0.0D;
    }

    public ConsSalesStmtRpt_2(OutputStream out_stream)
    {
        super(out_stream);
        USER_ID = null;
        COY_SUB = null;
        SUPPL = null;
        SUPPL_CONT = null;
        FR_CONCESS_DATE = null;
        TO_CONCESS_DATE = null;
        SALES_PERIOD = null;
        LOGO_NAME = "";
        LOGO_ADDR1 = "";
        LOGO_ADDR2 = "";
        LOGO_TEL_FAX = "";
        LOGO_GST_REG_NO = "";
        strDateFormat = "dd/MM/yyyy";
        concessQueryBuilder = null;
        sheet = null;
        workBook = null;
        headerRow = null;
        headerRowSub = null;
        headerCell = null;
        superheaderstyle = null;
        centerheaderstyle = null;
        headerstyle = null;
        cellstyle = null;
        cellAmountstyle = null;
        cellTtlAmountstyle = null;
        cellNumberstyle = null;
        cellMarginstyle = null;
        cellTotalstyle = null;
        cellGrdTtlstyle = null;
        region = null;
        dateFormatter = null;
        pstmt = null;
        psCmhdr = null;
        psHdr = null;
        resultSet = null;
        rsComm = null;
        rsHdr = null;
        profitvvSQL = null;
        coymstSQL = null;
        coysubmstSQL = null;
        strmstSQL = null;
        classmstSQL = null;
        itemmstSQL = null;
        vnmstrSQL = null;
        pohdrSQL = null;
        start_row = 0;
        begin_row = 0;
        temp_row = 0;
        SYSCompanyLogo = "";
        SYSReportLine = "";
        SYSDefaultLanguage = "";
        SYSMaxLinesPerSheet = "";
        SYSB2BPoAdjConcess = "";
        SYSCurrCom = "";
        SYSGenInvNo = ""; //added by Chiew 2017-02-19
        SYSVRInvFormula = ""; //added by Chiew 2017-02-19
        SYSCtrlGRNInv = ""; //added by Chiew 2017-02-19
        maxPerSheet = 0;
        currentRow = 0;
        noSheet = 2;
        countArray = 0;
        intTitleShort = 300;
        intTotalColumn = 22 + 14;
        intTtlByPlace = 6;
        intLogoSize = 7;
        intTitleSize = 1;
        intHeaderSize = 7;
        intTableSize = 2;
        intLineMover = 0;
        intLogoMover = 0;
        intTitleMover = 0;
        intHeaderMover = 0;
        intTableMover = 0;
        intDataMover = 0;
        dblTotSellWovat = 0.0D;
        dblTotSellVat = 0.0D;
        dblTotCostAmount = 0.0D;
        dblTotCommWovat = 0.0D;
        dblTotCommVat = 0.0D;
        dblTotCommWvat = 0.0D;
        dblTotCostWovat = 0.0D;
        dblTotCostVat = 0.0D;
        dblTotCostWvat = 0.0D;
        dblGrdTotSellWovat = 0.0D;
        dblGrdTotSellVat = 0.0D;
        dblGrdTotCostAmount = 0.0D;
        dblGrdTotCommWovat = 0.0D;
        dblGrdTotCommVat = 0.0D;
        dblGrdTotCommWvat = 0.0D;
        dblGrdTotCostWovat = 0.0D;
        dblGrdTotCostVat = 0.0D;
        dblGrdTotCostWvat = 0.0D;
    }

    private void jInit(HParam hParam)
        throws Exception
    {
        USER_ID = hParam.getString("USER_ID");
        USER_COY = hParam.getString("COY");
        COY_SUB = hParam.getString("COY_SUB");
        SUPPL = hParam.getString("SUPPL");
        SUPPL_CONT = hParam.getString("SUPPL_CONT");
        FR_CONCESS_DATE = hParam.getString("FR_CONCESS_DATE");
        TO_CONCESS_DATE = hParam.getString("TO_CONCESS_DATE");
        SALES_PERIOD = "";
        USER_LANGUAGE = retrieveUserLanguage(USER_ID);
        workBook = new HSSFWorkbook();
        profitvvSQL = new ProfitvvSQL(conn);
        coymstSQL = new CoymstSQL(conn);
        coysubmstSQL = new CoysubmstSQL(conn);
        strmstSQL = new StrmstSQL(conn);
        itemmstSQL = new ItemmstSQL(conn);
        classmstSQL = new ClassmstSQL(conn);
        vnmstrSQL = new VnmstrSQL(conn);
        pohdrSQL = new PohdrSQL(conn);
        adlangmstSQL = new AdlangmstSQL(conn);
        taxrptctrlSQL = new TaxrptctrlSQL(conn);
        
        concessQueryBuilder = new ConsSalesStmtRptQueryBuilder_2(conn);
        dateFormatter = new SimpleDateFormat("HH:mm:ss");
        webCurrConverter = new CurrencyConverter();
        SYSCompanyLogo = getProfitvvValue("SYSCompanyLogo");
        SYSReportLine = getProfitvvValue("SYSReportLine");
        SYSDefaultLanguage = getProfitvvValue("SYSDefaultLanguage");
        SYSMaxLinesPerSheet = getProfitvvValue("SYSMaxLinesPerSheet");
        SYSB2BPoAdjConcess = getProfitvvValue("SYSB2BPoAdjConcess");
        SYSCurrCom = getProfitvvValue("SYSCurrCom");
        SYSGenInvNo = getProfitvvValue("SYSGenInvNo"); //added by Chiew 2017-02-19
        SYSVRInvFormula = getProfitvvValue("SYSVRInvFormula"); //added by Chiew 2017-02-19
        SYSCtrlGRNInv = getProfitvvValue("SYSCtrlGRNInv"); //added by Chiew 2017-02-19
        SYSPrintConSlsStmt = getProfitvvValue("SYSPrintConSlsStmt"); //added by Chiew 2017-02-27
        maxPerSheet = Integer.parseInt(SYSMaxLinesPerSheet);
        USER_LANGUAGE = SYSDefaultLanguage;
        setGroupingForReport(USER_COY);
        getCOYMST(USER_COY);
        getCOYSUBMST(USER_COY, COY_SUB);
        if (SYSPrintConSlsStmt != null && SYSPrintConSlsStmt.equals("AVN"))
        {
          LOGO_NAME = AltDescUtil.getDesc("0", coysubmstSQL.COY_SUB_NAME()); //customer Ms Hoa wants to default to "AEON VIETNAM CO., LTD"
          LOGO_ADDR1 = "Head Office: " + AltDescUtil.getDesc(USER_LANGUAGE, coysubmstSQL.COYSUB_ADDR1()) + " " + AltDescUtil.getDesc(USER_LANGUAGE, coysubmstSQL.COYSUB_ADDR2());
          LOGO_ADDR2 = AltDescUtil.getDesc(USER_LANGUAGE, coysubmstSQL.COYSUB_ADDR3());
          
          if (LOGO_ADDR2 != null && LOGO_ADDR2.length() > 0)
            LOGO_ADDR2 = LOGO_ADDR2 + ", "  + AltDescUtil.getDesc(USER_LANGUAGE, coysubmstSQL.COYSUB_CITY());
          else 
            LOGO_ADDR1 = LOGO_ADDR1 + ", "  + AltDescUtil.getDesc(USER_LANGUAGE, coysubmstSQL.COYSUB_CITY());
              
          LOGO_TEL_FAX = "Tel: " + coysubmstSQL.COYSUB_PHONE() + " Fax: " + coysubmstSQL.COYSUB_FAX();
        }
        else
        {
          LOGO_NAME = AltDescUtil.getDesc(USER_LANGUAGE, coymstSQL.COY_NAME()) + ". (126926-H)";
          LOGO_ADDR1 = "Head Office: " + AltDescUtil.getDesc(USER_LANGUAGE, coymstSQL.COY_ADDR1()) + " " + AltDescUtil.getDesc(USER_LANGUAGE, coymstSQL.COY_ADDR2());
          LOGO_ADDR2 = AltDescUtil.getDesc(USER_LANGUAGE, coymstSQL.COY_ADDR3());
          LOGO_TEL_FAX = "Tel: " + coymstSQL.COY_PHONE() + " Fax: " + coymstSQL.COY_FAX();
        }
        System.out.println("coymstSQL.COY_TAX_REG_NO() = "+coymstSQL.COY_TAX_REG_NO());
        System.out.println("GST = "+adlangmstSQL.getTranslatedCaptionMsg(SYSDefaultLanguage,"GST"));
        LOGO_GST_REG_NO = adlangmstSQL.getTranslatedCaptionMsg(SYSDefaultLanguage,"GST") + " Reg. No.: " + coymstSQL.COY_TAX_REG_NO();
    }

    public void print(HParam hParam)
  {
    try
    {
      openOutputStream(); 
      openConnection();
      
      jInit(hParam);       
      System.out.println("start writing the report...");
      start();
      workBook.write(super.outStream);
      System.out.println("end of writing the report...");    
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      closeStatement();
      closeConnection();
      super.closeOutputStream();
    }
  }

    public void start()
        throws Exception
    {
        try
        {
            setSuperHeaderStyle();
            setCenterHeaderStyle();
            setHeaderStyle();
            setForCellStyle();
            setForCellAmountStyle();
            setForCellTtlAmountStyle();
            setForCellNumberStyle();
            setForCellMarginStyle();
            setForCellTotalStyle();
            setGrdTtlStyle();
            createConcessSheet();
        }
        catch(Exception Ex)
        {
            Ex.printStackTrace();
            throw Ex;
        }
    }

    private void reportLineMover()
        throws Exception
    {
        intLogoMover = intLineMover;
        intTitleMover = intLogoMover + intLogoSize;
        intHeaderMover = intTitleMover + intTitleSize;
        intTableMover = intHeaderMover + intHeaderSize;
        intDataMover = intTableMover + intTableSize;
    }

    public void createConcessSheet() throws Exception {
        reportLineMover();
        String strConcessQuery = "";
        String strNewContract = "";
        String strOldContract = "";
        String strNewPROCESSED_DATE = "";
        String strOldPROCESSED_DATE = "" ;
        String strNewDocumentID = "";
        String strOldDocumentID = "";
        String strOldSuppl = "";
        String strNewSuppl = "";
        int intTabNo = 1;
        
        try {
            boolean create_hdr = true; 
            int noSheet = 2;
            strConcessQuery = concessQueryBuilder.getConcessQuery(USER_COY, COY_SUB, SUPPL, SUPPL_CONT, FR_CONCESS_DATE, TO_CONCESS_DATE);
//            System.out.println("strConcessQuery = " + strConcessQuery);
            pstmt = conn.prepareStatement(strConcessQuery);
            resultSet = pstmt.executeQuery();
            headerRow = null;
            headerRowSub = null;
            begin_row = start_row = intDataMover;
            String strCommissionQuery = concessQueryBuilder.getCommissionQuery();
            psCmhdr = conn.prepareStatement(strCommissionQuery);
            
            getTaxRptFormat(USER_COY, COY_SUB, FR_CONCESS_DATE, TO_CONCESS_DATE);

            if (!resultSet.isBeforeFirst()) {
                sheet = workBook.createSheet("CONCESS");
                sheet.setColumnWidth((short) 0, (short) 4000); 
                if (SYSPrintConSlsStmt != null && SYSPrintConSlsStmt.equals("AVN"))
                  createHdrNoData("Consignment Sales Statement");
                else
                  createHdrNoData("Concessionaire Sales Statement");
                printNoData();
            } else {
                while (resultSet != null && resultSet.next()) {
                    int i;
                    pohdrSQL.setCOY(USER_COY);
                    pohdrSQL.setORD_NO(resultSet.getString("DOCUMENT_NO"));
                    if (pohdrSQL.getByKeyForUpdate() > 0) {
                        pohdrSQL.setPO_PRINTED_CNT(pohdrSQL.PO_PRINTED_CNT() + 1);
                        pohdrSQL.setLAST_OPR(USER_ID);
                        pohdrSQL.setLAST_OPR_FUNCT("PRINT_CS_SLS_STATMNT");
                        pohdrSQL.update();
                    }
                    strNewContract = resultSet.getString("CONTRACT");
                    strNewDocumentID = resultSet.getString("DOCUMENT_NO");
                    strNewPROCESSED_DATE = resultSet.getString("PROCESSED_DATE");
                    if (create_hdr)
                    {
                      strNewContract   = resultSet.getString("CONTRACT");
                      strOldContract   = strNewContract;
                      strNewDocumentID = resultSet.getString("DOCUMENT_NO");
                      strOldDocumentID = strNewDocumentID;
                      strNewPROCESSED_DATE = resultSet.getString("PROCESSED_DATE");
                      strOldPROCESSED_DATE = strNewPROCESSED_DATE;
                    }
                    
                    strNewSuppl = resultSet.getString("SUPPL");
                    //System.out.println("strNewSuppl = "+strNewSuppl);
                    if(strNewSuppl == null)
                    {
                      strNewSuppl = "";
                    }
                    if(strOldSuppl.equals(strNewSuppl) == false)
                    {
                    System.out.println("new page");
                      strOldSuppl = strNewSuppl;
                    }
                    
                    if(strNewDocumentID == null)
                    {
                      strNewDocumentID = "";
                    }
                    
                    if(resultSet.getString("TYPE").equals("DOCUMENT TOTAL"))
                    {
                    
                    headerRow = sheet.createRow(start_row);
                    createEmptyRowConcess(start_row, headerRow, resultSet);
                    ++start_row;
                    ++currentRow;
                    }
                    
                    if (!strNewDocumentID.equals(strOldDocumentID)) {
                        headerRow = sheet.createRow(start_row);
                        createEmptyRowConcess(start_row, headerRow, resultSet);
                        ++start_row;
                        ++currentRow;

                        strOldDocumentID = strNewDocumentID;
                    }

                    if (currentRow >= maxPerSheet || !strNewContract.equals(strOldContract) || !strNewPROCESSED_DATE.equals(strOldPROCESSED_DATE))
                    {
                        if (currentRow >= maxPerSheet)
                            intTabNo++;
                        
                        currentRow = 0; //reset after increase intTabNo                                              
                        
                        if (!strNewContract.equals(strOldContract) || !strNewPROCESSED_DATE.equals(strOldPROCESSED_DATE))
                            intTabNo = 1;                        
                        
                        if (intTabNo == 1)
                            sheet = workBook.createSheet(resultSet.getString("PROCESSED_DATE").substring(0,10) + " " + resultSet.getString("CONTRACT"));
                        else
                            sheet = workBook.createSheet(resultSet.getString("PROCESSED_DATE").substring(0,10) + " " + resultSet.getString("CONTRACT")+ "_" + String.valueOf(intTabNo));
                            
                        sheet.setColumnWidth((short) 0, (short) 4000);
                        sheet.setColumnWidth((short) 1, (short) 3500);
                        sheet.setColumnWidth((short) 2, (short) 10000);
                        sheet.setColumnWidth((short) 3, (short) 3500);
                        sheet.setColumnWidth((short) 4, (short) 10000);
                        sheet.setColumnWidth((short) 5, (short) 3500);
                        sheet.setColumnWidth((short) 6, (short) 10000);
                        sheet.setColumnWidth((short) 7, (short) 3500);
                        for (i = 8; i < intTotalColumn; ++i) {
                            sheet.setColumnWidth((short) i, (short) 4000);
                        }
                        if(taxRptFormat.equals("1")) {
                            sheet.setColumnWidth((short) 13, (short) 6000);
                            sheet.setColumnWidth((short) 24, (short) 6000);
                            sheet.setColumnWidth((short) 29, (short) 6000);
                        } else {
                            sheet.setColumnWidth((short) 11, (short) 6000);
                            sheet.setColumnWidth((short) 15, (short) 8000);
                            sheet.setColumnWidth((short) 16, (short) 6000);
                            sheet.setColumnWidth((short) 18, (short) 6000);
                        }
                        
                        
                        createHdrConcess();
                        ++noSheet;
                        start_row = intDataMover;
                        strOldContract = strNewContract;
                        strOldPROCESSED_DATE = strNewPROCESSED_DATE;
                    }
                    if (create_hdr) {
                        //sheet = workBook.createSheet(resultSet.getString("PROCESSED_DATE").substring(0,10) + " " + resultSet.getString("CONTRACT"));
                        
                        if (intTabNo == 1)
                            sheet = workBook.createSheet(resultSet.getString("PROCESSED_DATE").substring(0,10) + " " + resultSet.getString("CONTRACT"));
                        else
                            sheet = workBook.createSheet(resultSet.getString("PROCESSED_DATE").substring(0,10) + " " + resultSet.getString("CONTRACT")+ "_" + String.valueOf(intTabNo));
                        sheet.setColumnWidth((short) 0, (short) 4000);
                        sheet.setColumnWidth((short) 1, (short) 3500);
                        sheet.setColumnWidth((short) 2, (short) 10000);
                        sheet.setColumnWidth((short) 3, (short) 3500);
                        sheet.setColumnWidth((short) 4, (short) 10000);
                        sheet.setColumnWidth((short) 5, (short) 3500);
                        sheet.setColumnWidth((short) 6, (short) 10000);
                        sheet.setColumnWidth((short) 7, (short) 3500);
                        for (i = 8; i < intTotalColumn; ++i) {
                            sheet.setColumnWidth((short) i, (short) 4000);
                        }
                        if(taxRptFormat.equals("1")) {
                            sheet.setColumnWidth((short) 13, (short) 6000);
                            sheet.setColumnWidth((short) 24, (short) 6000);
                            sheet.setColumnWidth((short) 29, (short) 6000);
                        } else {
                            sheet.setColumnWidth((short) 11, (short) 6000);
                            sheet.setColumnWidth((short) 15, (short) 8000);
                            sheet.setColumnWidth((short) 16, (short) 6000);
                            sheet.setColumnWidth((short) 18, (short) 6000);
                        }
                        
                        createHdrConcess();
                        create_hdr = false;
                    }
                    headerRow = null;
                    headerRow = sheet.createRow(start_row);
                    createDataConcess(headerRow, resultSet);
                    ++start_row;
                    ++currentRow;
                }
//                headerRow = sheet.createRow(start_row);
//                createEmptyRowConcess(start_row, headerRow, resultSet);
//                ++start_row;
//                ++currentRow;
//                
//
//                
//                headerRow = sheet.createRow(start_row);
//                createEmptyRowConcess(start_row, headerRow, resultSet);
//                ++start_row;
//                ++currentRow;
//                headerRow = sheet.createRow(start_row);
//                createTotalConcess(start_row, headerRow, resultSet);
//                ++start_row;
//                ++currentRow;
                
//                headerRow = sheet.createRow(start_row);
//                createGrandTotalConcess(start_row, headerRow, resultSet);
//                ++start_row;
//                ++currentRow;
            }
            conn.commit();
        }
        catch (Exception ex) {
            conn.rollback();
            ex.printStackTrace();
            throw ex;
        }
    }
     
    private void getTaxRptFormat(String strCoy, String strCoySub, String strFrConcessDate, String strToConcessDate) throws SQLException {
        StatementManager manager = null;
        String query;
        String strDateCtrl;
        Date updatedDate = null;
        taxrptctrlSQL.setDOC_TYPE(DOC_TYPE);

        Date first_day = qrMisc.parseSqlDate(strFrConcessDate);
        Date last_day = qrMisc.parseSqlDate(strToConcessDate);

        if (taxrptctrlSQL.getDateCtrlByDocType() > 0) {
            strDateCtrl = taxrptctrlSQL.DATE_CTRL();
            if (strDateCtrl != null) {
                try {
                    manager = StatementManager.newInstance(conn);
                    query =
                        "SELECT " + strDateCtrl + " FROM CSSDOC WHERE COY = '" + strCoy + "' AND COY_SUB = '" +
                        strCoySub + "'" + " AND UPDATE_DATE BETWEEN TO_DATE('" + first_day +
                        "','yyyy-MM-dd') AND TO_DATE('" + last_day + "','yyyy-MM-dd')";
                    
                    ResultSet rs = manager.select(query);

                    if (rs != null && rs.next()) {
                        updatedDate = rs.getDate(strDateCtrl);
                    }

                    if (updatedDate != null) {
                        if (taxrptctrlSQL.getByDateRange(updatedDate) > 0) {
                            taxRptFormat = taxrptctrlSQL.RPT_FORMAT();
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
        
    private void createHdrConcess()
        throws Exception
    {
        String strHeaderQuery = "";
        String strDocNo = resultSet.getString("DOCUMENT_NO");
        String strStore = resultSet.getString("STORE");
        String strSuppl = resultSet.getString("SUPPL");
        String strSupplCont = resultSet.getString("CONTRACT");

        try
        {
            strHeaderQuery = concessQueryBuilder.getConcessHeaderQuery(strDocNo, strStore, strSuppl, strSupplCont);
            psHdr = conn.prepareStatement(strHeaderQuery);
            rsHdr = psHdr.executeQuery();
            
            
            if(rsHdr != null && rsHdr.next())
                SALES_PERIOD = FR_CONCESS_DATE + " - " + TO_CONCESS_DATE;
            else
                SALES_PERIOD = "";
            getVNMSTR(strSuppl);
            if (SYSPrintConSlsStmt != null && SYSPrintConSlsStmt.equals("AVN"))
              createHdrData("Consignment Sales Statement");
            else
              createHdrData("Concessionaire Sales Statement");
            createTblHdrConcess();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    public void createHdrNoData(String REPORT_HDR)
        throws Exception
    {
        int headerCount = 0;
        int columnCount = 0;
        String strLogoName = LOGO_NAME;
        String strLogoAddr1 = LOGO_ADDR1;
        String strLogoAddr2 = LOGO_ADDR2;
        String strLogoTelFax = LOGO_TEL_FAX;
        String strLogoGSTRegNo;
        String coy_logo_image_file = SYSCompanyLogo;
        String hdr_line_image_file = SYSReportLine;
        
        if(taxRptFormat.equals("1")) {
            strLogoGSTRegNo = LOGO_GST_REG_NO;
        } else {
            strLogoGSTRegNo = "";
        }
        
        FileInputStream fis1 = new FileInputStream(coy_logo_image_file);
        FileInputStream fis2 = new FileInputStream(hdr_line_image_file);
        ByteArrayOutputStream img_bytes1 = new ByteArrayOutputStream();
        ByteArrayOutputStream img_bytes2 = new ByteArrayOutputStream();
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
        
        int logoCount;
        while((logoCount = fis1.read()) != -1) 
            img_bytes1.write(logoCount);
        fis1.close();
        int lineCount;
        while((lineCount = fis2.read()) != -1) 
            img_bytes2.write(lineCount);
        fis2.close();
        HSSFClientAnchor anchor1 = new HSSFClientAnchor();
        anchor1.setAnchor((short)0, 0, 0, 0, (short)2, 4, 0, 0);
        anchor1.setAnchorType(3);
        HSSFClientAnchor anchor2 = new HSSFClientAnchor();
        anchor2.setAnchor((short)2, 1, 0, 0, (short)13, 2, 0, 0);
        anchor2.setAnchorType(3);
        int index1 = workBook.addPicture(img_bytes1.toByteArray(), 5);
        int index2 = workBook.addPicture(img_bytes2.toByteArray(), 5);
        patriarch.createPicture(anchor1, index1);
        patriarch.createPicture(anchor2, index2);
        headerRowSub = sheet.createRow((short)intLogoMover + 1);
        headerRowSub.setHeight((short)100);
        
        headerCount = 0;
        headerRow = null;
        headerRow = sheet.createRow((short)intLogoMover + headerCount);
        region = new Region(intLogoMover + headerCount, (short)2, intLogoMover + headerCount, (short)12);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRow.createCell((short)2);
        headerCell.setCellValue(strLogoName);
        headerCell.setCellStyle(superheaderstyle);
        region = new Region(intLogoMover + headerCount, (short)2, intLogoMover + headerCount, (short)12);
        sheet.addMergedRegion(region);
        
        headerCell = null;
        headerCell = headerRowSub.createCell((short)(headerCount++));
        headerCell.setCellValue("");
        headerCell.setCellStyle(superheaderstyle);
        headerCount++;
        
        headerRow = null;
        headerRow = sheet.createRow((short)intLogoMover + headerCount);
        region = new Region(intLogoMover + headerCount, (short)2, intLogoMover + headerCount, (short)12);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRow.createCell((short)2);
        headerCell.setCellValue(strLogoAddr1);
        headerCell.setCellStyle(superheaderstyle);
        headerCount++;
        
        headerRow = null;
        headerRow = sheet.createRow((short)intLogoMover + headerCount);
        region = new Region(intLogoMover + headerCount, (short)2, intLogoMover + headerCount, (short)12);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRow.createCell((short)2);
        headerCell.setCellValue(strLogoAddr2);
        headerCell.setCellStyle(superheaderstyle);
        headerCount++;
        
        headerRow = null;
        headerRow = sheet.createRow((short)intLogoMover + headerCount);
        region = new Region(intLogoMover + headerCount, (short)2, intLogoMover + headerCount, (short)12);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRow.createCell((short)2);
        headerCell.setCellValue(strLogoTelFax);
        headerCell.setCellStyle(superheaderstyle);
        headerCount++;
        
        headerRow = null;
        headerRow = sheet.createRow((short)intLogoMover + headerCount);
        region = new Region(intLogoMover + headerCount, (short)2, intLogoMover + headerCount, (short)12);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRow.createCell((short)2);
        headerCell.setCellValue(strLogoGSTRegNo);
        headerCell.setCellStyle(superheaderstyle);
        headerRow = null;
        headerRow = sheet.createRow((short)intTitleMover);
        region = new Region(intTitleMover, (short)0, intTitleMover, (short)12);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRow.createCell((short)0);
        headerCell.setCellValue(REPORT_HDR);
        headerCell.setCellStyle(centerheaderstyle);
    }

    public void createHdrData(String REPORT_HDR)
        throws Exception
    {
    
        int headerCount = 0;
        int columnCount = 0;
        String strLogoName = LOGO_NAME;
        String strLogoAddr1 = LOGO_ADDR1;
        String strLogoAddr2 = LOGO_ADDR2;
        String strLogoTelFax = LOGO_TEL_FAX;
        String strLogoGSTRegNo;
        String strCompName = AltDescUtil.getDesc(USER_LANGUAGE, vnmstrSQL.VN_NAME());
        String strSupplier = vnmstrSQL.VN_CODE();
        
        if(taxRptFormat.equals("1")) {
            strLogoGSTRegNo = LOGO_GST_REG_NO;
        } else {
            strLogoGSTRegNo = "";
        }
        
        String strContract = resultSet.getString("CONTRACT");
        String strProcessedDate = resultSet.getString("PROCESSED_DATE").substring(0,10);
        String strStartDate = resultSet.getString("sales_start_date").substring(0,10);
        String strEndDate = resultSet.getString("sales_end_date").substring(0,10);
        String strFrConcessDate = FR_CONCESS_DATE;
        String strToConcessDate = TO_CONCESS_DATE;
        String strSalesPeriod = strStartDate + " - " + strEndDate;
        String coy_logo_image_file = SYSCompanyLogo;
        String hdr_line_image_file = SYSReportLine;
        FileInputStream fis1 = new FileInputStream(coy_logo_image_file);
        FileInputStream fis2 = new FileInputStream(hdr_line_image_file);
        ByteArrayOutputStream img_bytes1 = new ByteArrayOutputStream();
        ByteArrayOutputStream img_bytes2 = new ByteArrayOutputStream();
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
        int logoCount;
        while((logoCount = fis1.read()) != -1) 
            img_bytes1.write(logoCount);
        fis1.close();
        int lineCount;
        while((lineCount = fis2.read()) != -1) 
            img_bytes2.write(lineCount);
        fis2.close();
        HSSFClientAnchor anchor1 = new HSSFClientAnchor();
        anchor1.setAnchor((short)0, 0, 0, 0, (short)2, 4, 0, 0);
        anchor1.setAnchorType(3);
        HSSFClientAnchor anchor2 = new HSSFClientAnchor();
        anchor2.setAnchor((short)2, 1, 0, 0, (short)11, 2, 0, 0);
        anchor2.setAnchorType(3);
        int index1 = workBook.addPicture(img_bytes1.toByteArray(), 5);
        int index2 = workBook.addPicture(img_bytes2.toByteArray(), 5);
        patriarch.createPicture(anchor1, index1);
        patriarch.createPicture(anchor2, index2);
        headerRowSub = sheet.createRow((short)intLogoMover + 1);
        headerRowSub.setHeight((short)100);
        headerCount = 0;
        headerRow = null;
        headerRow = sheet.createRow((short)intLogoMover + headerCount);
        region = new Region(intLogoMover + headerCount, (short)2, intLogoMover + headerCount, (short)4);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRow.createCell((short)2);
        headerCell.setCellValue(strLogoName);
        headerCell.setCellStyle(superheaderstyle);
        region = new Region(intLogoMover + headerCount, (short)2, intLogoMover + headerCount, (short)4);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRowSub.createCell((short)(headerCount++));
        headerCell.setCellValue("");
        headerCell.setCellStyle(superheaderstyle);
        headerCount++;
        headerRow = null;
        headerRow = sheet.createRow((short)intLogoMover + headerCount);
        region = new Region(intLogoMover + headerCount, (short)2, intLogoMover + headerCount, (short)4);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRow.createCell((short)2);
        headerCell.setCellValue(strLogoAddr1);
        headerCell.setCellStyle(superheaderstyle);
        headerCount++;
        headerRow = null;
        headerRow = sheet.createRow((short)intLogoMover + headerCount);
        region = new Region(intLogoMover + headerCount, (short)2, intLogoMover + headerCount, (short)4);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRow.createCell((short)2);
        headerCell.setCellValue(strLogoAddr2);
        headerCell.setCellStyle(superheaderstyle);
        headerCount++;
        headerRow = null;
        headerRow = sheet.createRow((short)intLogoMover + headerCount);
        region = new Region(intLogoMover + headerCount, (short)2, intLogoMover + headerCount, (short)4);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRow.createCell((short)2);
        headerCell.setCellValue(strLogoTelFax);
        headerCell.setCellStyle(superheaderstyle);
        headerCount++;
        headerRow = null;
        headerRow = sheet.createRow((short)intLogoMover + headerCount);
        region = new Region(intLogoMover + headerCount, (short)2, intLogoMover + headerCount, (short)4);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRow.createCell((short)2);
        headerCell.setCellValue(strLogoGSTRegNo);
        headerCell.setCellStyle(superheaderstyle);
        headerRow = null;
        headerRow = sheet.createRow((short)intTitleMover);
        region = new Region(intTitleMover, (short)0, intTitleMover, (short)10);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRow.createCell((short)0);
        headerCell.setCellValue(REPORT_HDR);
        headerCell.setCellStyle(centerheaderstyle);
        columnCount = 0;
        headerRow = null;
        headerRow = sheet.createRow((short)intHeaderMover + columnCount);
        region = new Region(intHeaderMover + columnCount, (short)0, intHeaderMover + columnCount, (short)0);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRow.createCell((short)0);
        headerCell.setCellValue("");
        headerCell.setCellStyle(superheaderstyle);
        columnCount++;
        headerRow = null;
        headerRow = sheet.createRow((short)intHeaderMover + columnCount);
        region = new Region(intHeaderMover + columnCount, (short)0, intHeaderMover + columnCount, (short)1);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRow.createCell((short)0);
        headerCell.setCellValue("To :");
        headerCell.setCellStyle(superheaderstyle);
        region = new Region(intHeaderMover + columnCount, (short)2, intHeaderMover + columnCount, (short)6);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRow.createCell((short)2);
        headerCell.setCellValue(strCompName);
        headerCell.setCellStyle(superheaderstyle);
        columnCount++;
        headerRow = null;
        headerRow = sheet.createRow((short)intHeaderMover + columnCount);
        region = new Region(intHeaderMover + columnCount, (short)0, intHeaderMover + columnCount, (short)1);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRow.createCell((short)0);
        headerCell.setCellValue("Supplier Code :");
        headerCell.setCellStyle(superheaderstyle);
        region = new Region(intHeaderMover + columnCount, (short)2, intHeaderMover + columnCount, (short)6);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRow.createCell((short)2);
        headerCell.setCellValue(strSupplier);
        headerCell.setCellStyle(superheaderstyle);
        columnCount++;
        headerRow = null;
        headerRow = sheet.createRow((short)intHeaderMover + columnCount);
        region = new Region(intHeaderMover + columnCount, (short)0, intHeaderMover + columnCount, (short)1);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRow.createCell((short)0);
        headerCell.setCellValue("Contract Code :");
        headerCell.setCellStyle(superheaderstyle);
        region = new Region(intHeaderMover + columnCount, (short)2, intHeaderMover + columnCount, (short)6);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRow.createCell((short)2);
        headerCell.setCellValue(strContract);
        headerCell.setCellStyle(superheaderstyle);
        columnCount++;
        headerRow = null;
        headerRow = sheet.createRow((short)intHeaderMover + columnCount);
        region = new Region(intHeaderMover + columnCount, (short)0, intHeaderMover + columnCount, (short)1);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRow.createCell((short)0);
        headerCell.setCellValue("Date :");
        headerCell.setCellStyle(superheaderstyle);
        region = new Region(intHeaderMover + columnCount, (short)2, intHeaderMover + columnCount, (short)6);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRow.createCell((short)2);
        headerCell.setCellValue(strProcessedDate);
        headerCell.setCellStyle(superheaderstyle);
        columnCount++;
        headerRow = null;
        headerRow = sheet.createRow((short)intHeaderMover + columnCount);
        region = new Region(intHeaderMover + columnCount, (short)0, intHeaderMover + columnCount, (short)1);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRow.createCell((short)0);
        headerCell.setCellValue("Sales Period :");
        headerCell.setCellStyle(superheaderstyle);
        region = new Region(intHeaderMover + columnCount, (short)2, intHeaderMover + columnCount, (short)6);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRow.createCell((short)2);
        headerCell.setCellValue(strSalesPeriod);
        headerCell.setCellStyle(superheaderstyle);
        columnCount++;
        headerRow = null;
        headerRow = sheet.createRow((short)intHeaderMover + columnCount);
        region = new Region(intHeaderMover + columnCount, (short)0, intHeaderMover + columnCount, (short)0);
        sheet.addMergedRegion(region);
        headerCell = null;
        headerCell = headerRow.createCell((short)0);
        headerCell.setCellValue("");
        headerCell.setCellStyle(superheaderstyle);
    }

    private void createTblHdrConcess()
        throws Exception
    {
        int columnCount = 0;
        headerRow = null;
        headerRow = sheet.createRow((short)intTableMover);
        headerRowSub = sheet.createRow((short)intTableMover + 1);
        headerRowSub.setHeight((short)1000);
        region = new Region(intTableMover, (short)columnCount, intTableMover + 1, (short)columnCount);
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        headerCell = null;
        headerCell = headerRow.createCell((short)(columnCount++));
        headerCell.setCellValue("Document ID");
        headerCell.setCellStyle(headerstyle);
        region = new Region(intTableMover, (short)columnCount, intTableMover + 1, (short)columnCount);
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        headerCell = null;
        headerCell = headerRow.createCell((short)(columnCount++));
        headerCell.setCellValue("Store Code");
        headerCell.setCellStyle(headerstyle);
        region = new Region(intTableMover, (short)columnCount, intTableMover + 1, (short)columnCount);
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        headerCell = null;
        headerCell = headerRow.createCell((short)(columnCount++));
        headerCell.setCellValue("Store Name");
        headerCell.setCellStyle(headerstyle);
        region = new Region(intTableMover, (short)columnCount, intTableMover + 1, (short)columnCount);
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        headerCell = null;
        headerCell = headerRow.createCell((short)(columnCount++));
        headerCell.setCellValue("Item Code");
        headerCell.setCellStyle(headerstyle);
        region = new Region(intTableMover, (short)columnCount, intTableMover + 1, (short)columnCount);
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        headerCell = null;
        headerCell = headerRow.createCell((short)(columnCount++));
        headerCell.setCellValue("Item Description");
        headerCell.setCellStyle(headerstyle);
        region = new Region(intTableMover, (short)columnCount, intTableMover + 1, (short)columnCount);
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        headerCell = null;
        headerCell = headerRow.createCell((short)(columnCount++));
        headerCell.setCellValue("Dept Code");
        headerCell.setCellStyle(headerstyle);
        region = new Region(intTableMover, (short)columnCount, intTableMover + 1, (short)columnCount);
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        headerCell = null;
        headerCell = headerRow.createCell((short)(columnCount++));
        headerCell.setCellValue("Dept Description");
        headerCell.setCellStyle(headerstyle);
        region = new Region(intTableMover, (short)columnCount, intTableMover + 1, (short)columnCount);
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        headerCell = null;
        headerCell = headerRow.createCell((short)(columnCount++));
        headerCell.setCellValue("Category Code");
        headerCell.setCellStyle(headerstyle);
        region = new Region(intTableMover, (short)columnCount, intTableMover + 1, (short)columnCount);
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        headerCell = null;
        
        headerCell = null;
        headerCell = headerRow.createCell((short)(columnCount++));
        headerCell.setCellValue("Type");
        headerCell.setCellStyle(headerstyle);
        region = new Region(intTableMover, (short)columnCount, intTableMover + 1, (short)columnCount);
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        
        headerCell = headerRow.createCell((short)(columnCount++));
        headerCell.setCellValue("Quantity");
        headerCell.setCellStyle(headerstyle);
        region = new Region(intTableMover, (short)columnCount, intTableMover + 1, (short)columnCount);
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        
        String strSalesAmountExcl;
        String strCostAmountExcl;
        String strAEONCommissionExcl;
        String strSupplierInvoiceAmountExcl;
        String strGRNAmountExcl;
        String strCreditCostAmount;
        String strCreditCommAmount;
        if(taxRptFormat.equals("1")) {
            strSalesAmountExcl = "Sales Amount Excl. " + adlangmstSQL.getTranslatedCaptionMsg(SYSDefaultLanguage,"GST") + " (" + SYSCurrCom + ")";
            strCostAmountExcl = "Cost Amount Excl. " + adlangmstSQL.getTranslatedCaptionMsg(SYSDefaultLanguage,"GST") + 
                                " (" + SYSCurrCom + ")";
            strAEONCommissionExcl = "AEON Commission Excl. " + adlangmstSQL.getTranslatedCaptionMsg(SYSDefaultLanguage,"GST") + " (" + SYSCurrCom + ")";
            strSupplierInvoiceAmountExcl = "Supplier Invoice Amount Excl. " + adlangmstSQL.getTranslatedCaptionMsg(SYSDefaultLanguage,"GST") +
                                           " (" + SYSCurrCom + ")";
            strGRNAmountExcl = "GRN Amount Excl. " + adlangmstSQL.getTranslatedCaptionMsg(SYSDefaultLanguage,"GST") +
                               " (" + SYSCurrCom + ")";
            strCreditCostAmount = "Cost Amount Excl. " + adlangmstSQL.getTranslatedCaptionMsg(SYSDefaultLanguage,"GST") +
                                  " (" + SYSCurrCom + ")";
            strCreditCommAmount = "Comm. Amount Excl. " + adlangmstSQL.getTranslatedCaptionMsg(SYSDefaultLanguage,"GST") +
                                  " (" + SYSCurrCom + ")";
        } else {
            strSalesAmountExcl = "Sales Amount (" + SYSCurrCom + ")";
            strCostAmountExcl = "Cost Amount (" + SYSCurrCom + ")";
            strAEONCommissionExcl = "AEON Commission (" + SYSCurrCom + ")";
            strSupplierInvoiceAmountExcl = "Supplier Invoice Amount (" + SYSCurrCom + ")";
            strGRNAmountExcl = "GRN Amount (" + SYSCurrCom + ")";
            strCreditCostAmount = "Cost Amount (" + SYSCurrCom + ")";
            strCreditCommAmount = "Comm. Amount (" + SYSCurrCom + ")";
        }
        
        headerCell = null;
        headerCell = headerRow.createCell((short)(columnCount++));
        headerCell.setCellValue(strSalesAmountExcl);
        headerCell.setCellStyle(headerstyle);
        
        if(taxRptFormat.equals("1")) {
            region = new Region(intTableMover, (short)columnCount, intTableMover + 1, (short)columnCount);
            sheet.addMergedRegion(region);
            setRegion((short)1, sheet, region, workBook);
            headerCell = headerRow.createCell((short)(columnCount++));
            headerCell.setCellValue(adlangmstSQL.getTranslatedCaptionMsg(SYSDefaultLanguage,"GST") + " Rate (%)");
            headerCell.setCellStyle(headerstyle);
            
            region = new Region(intTableMover, (short)columnCount, intTableMover + 1, (short)columnCount);
            sheet.addMergedRegion(region);
            setRegion((short)1, sheet, region, workBook);
            headerCell = headerRow.createCell((short)(columnCount++));
            headerCell.setCellValue(adlangmstSQL.getTranslatedCaptionMsg(SYSDefaultLanguage,"GST") + " Amount (" + SYSCurrCom + ")");
            headerCell.setCellStyle(headerstyle);
        }
        
        if(taxRptFormat.equals("1")) {
            region = new Region(intTableMover, (short)columnCount, intTableMover, (short)(columnCount + 6));
        } else {
            region = new Region(intTableMover, (short)columnCount, intTableMover, (short)(columnCount + 3));
        }
        
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        headerCell = null;
        headerCell = headerRow.createCell((short)columnCount);
        headerCell.setCellValue("FOR AEON TAX INVOICE TO SUPPLIER");
        headerCell.setCellStyle(headerstyle);
        
        region = new Region(intTableMover + 1, (short)columnCount, intTableMover + 1, (short)columnCount);
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        headerCell = null;
        headerCell = headerRowSub.createCell((short)(columnCount++));
        headerCell.setCellValue("Tax Invoice Number");
        headerCell.setCellStyle(headerstyle);
        
        region = new Region(intTableMover + 1, (short)columnCount, intTableMover + 1, (short)columnCount);
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        headerCell = null;
        headerCell = headerRowSub.createCell((short)(columnCount++));
        headerCell.setCellValue("Margin (%)");
        headerCell.setCellStyle(headerstyle);
        
        region = new Region(intTableMover + 1, (short)columnCount, intTableMover + 1, (short)columnCount);
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        headerCell = null;
        headerCell = headerRowSub.createCell((short)(columnCount++));
        headerCell.setCellValue(strCostAmountExcl);
        headerCell.setCellStyle(headerstyle);
        
        region = new Region(intTableMover + 1, (short)columnCount, intTableMover + 1, (short)columnCount);
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        headerCell = null;
        headerCell = headerRowSub.createCell((short)(columnCount++));
        headerCell.setCellValue(strAEONCommissionExcl);
        headerCell.setCellStyle(headerstyle);
        
        if(taxRptFormat.equals("1")) {
            region = new Region(intTableMover + 1, (short)columnCount, intTableMover + 1, (short)columnCount);
            sheet.addMergedRegion(region);
            setRegion((short)1, sheet, region, workBook);
            headerCell = null;
            headerCell = headerRowSub.createCell((short)(columnCount++));
            headerCell.setCellValue("AEON Commission Tax Rate (%)");
            headerCell.setCellStyle(headerstyle);
            
            region = new Region(intTableMover + 1, (short)columnCount, intTableMover + 1, (short)columnCount);
            sheet.addMergedRegion(region);
            setRegion((short)1, sheet, region, workBook);
            headerCell = null;
            headerCell = headerRowSub.createCell((short)(columnCount++));
            headerCell.setCellValue(adlangmstSQL.getTranslatedCaptionMsg(SYSDefaultLanguage,"GST") + "  Amount (" + SYSCurrCom + ")");
            headerCell.setCellStyle(headerstyle);
            
            region = new Region(intTableMover + 1, (short)columnCount, intTableMover + 1, (short)columnCount);
            sheet.addMergedRegion(region);
            setRegion((short)1, sheet, region, workBook);
            headerCell = null;
            headerCell = headerRowSub.createCell((short)(columnCount++));
            headerCell.setCellValue("AEON Commission Amount Incl. " + adlangmstSQL.getTranslatedCaptionMsg(SYSDefaultLanguage,"GST") + " (" + SYSCurrCom + ")");
            headerCell.setCellStyle(headerstyle);
        }
        
        if(taxRptFormat.equals("1")) {
            region = new Region(intTableMover, (short)columnCount, intTableMover, (short)(columnCount + 3));
        } else {
            region = new Region(intTableMover, (short)columnCount, intTableMover, (short)(columnCount));
        }
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        headerCell = null;
        headerCell = headerRow.createCell((short)columnCount);
        headerCell.setCellValue("FOR SUPPLIER TAX INVOICE TO AEON");
        headerCell.setCellStyle(headerstyle);
        
        region = new Region(intTableMover, (short)columnCount, intTableMover, (short)columnCount);
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        headerCell = null;
        headerCell = headerRowSub.createCell((short)(columnCount++));
        headerCell.setCellValue(strSupplierInvoiceAmountExcl);
        headerCell.setCellStyle(headerstyle);
        
        if(taxRptFormat.equals("1")) {
            region = new Region(intTableMover, (short)columnCount, intTableMover, (short)columnCount);
            sheet.addMergedRegion(region);
            setRegion((short)1, sheet, region, workBook);
            headerCell = null;
            headerCell = headerRowSub.createCell((short)(columnCount++));
            headerCell.setCellValue("Supplier " + adlangmstSQL.getTranslatedCaptionMsg(SYSDefaultLanguage,"GST") + " Rate (%)");
            headerCell.setCellStyle(headerstyle);
            
            region = new Region(intTableMover, (short)columnCount, intTableMover, (short)columnCount);
            sheet.addMergedRegion(region);
            setRegion((short)1, sheet, region, workBook);
            headerCell = null;
            headerCell = headerRowSub.createCell((short)(columnCount++));
            headerCell.setCellValue(adlangmstSQL.getTranslatedCaptionMsg(SYSDefaultLanguage,"GST") + "  Amount (" + SYSCurrCom + ")");
            headerCell.setCellStyle(headerstyle);
            
            region = new Region(intTableMover, (short)columnCount, intTableMover, (short)columnCount);
            sheet.addMergedRegion(region);
            setRegion((short)1, sheet, region, workBook);
            headerCell = null;
            headerCell = headerRowSub.createCell((short)(columnCount++));
            headerCell.setCellValue("Supplier Invoice Amount Incl. " + adlangmstSQL.getTranslatedCaptionMsg(SYSDefaultLanguage,"GST") + " (" + SYSCurrCom + ")");
            headerCell.setCellStyle(headerstyle);
        }
        
        if(taxRptFormat.equals("1")) {
            region = new Region(intTableMover, (short)columnCount, intTableMover, (short)(columnCount + 4));
        } else {
            region = new Region(intTableMover, (short)columnCount, intTableMover, (short)(columnCount + 1));
        }
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        headerCell = null;
        headerCell = headerRow.createCell((short)columnCount);
        if (SYSCtrlGRNInv != null && !SYSCtrlGRNInv.equals("Y")) //Condition added by Chiew : Added 2017-02-19
            headerCell.setCellValue("FOR AEON DEBIT NOTE TO SUPPLIER");
        else
            headerCell.setCellValue("FOR AEON RESELL INVOICE TO SUPPLIER"); 
        headerCell.setCellStyle(headerstyle);
        
        region = new Region(intTableMover, (short)columnCount, intTableMover, (short)columnCount);
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        headerCell = null;
        headerCell = headerRowSub.createCell((short)(columnCount++));
        if (SYSCtrlGRNInv != null && !SYSCtrlGRNInv.equals("Y")) //Condition added by Chiew : Added 2017-02-19
            headerCell.setCellValue("Debit Note Number");
        else  
            headerCell.setCellValue("Resell Invoice Number"); //GRN Resell Tax Invoice No
            
        headerCell.setCellStyle(headerstyle);
        region = new Region(intTableMover, (short)columnCount, intTableMover, (short)columnCount);
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        headerCell = null;
        headerCell = headerRowSub.createCell((short)(columnCount++));
        headerCell.setCellValue(strGRNAmountExcl);
        headerCell.setCellStyle(headerstyle);
        
        if(taxRptFormat.equals("1")) {
            region = new Region(intTableMover, (short)columnCount, intTableMover, (short)columnCount);
            sheet.addMergedRegion(region);
            setRegion((short)1, sheet, region, workBook);
            headerCell = null;
            headerCell = headerRowSub.createCell((short)(columnCount++));
            headerCell.setCellValue(adlangmstSQL.getTranslatedCaptionMsg(SYSDefaultLanguage,"GST") + " Rate");
            headerCell.setCellStyle(headerstyle);
            
            region = new Region(intTableMover, (short)columnCount, intTableMover, (short)columnCount);
            sheet.addMergedRegion(region);
            setRegion((short)1, sheet, region, workBook);
            headerCell = null;
            headerCell = headerRowSub.createCell((short)(columnCount++));
            headerCell.setCellValue(adlangmstSQL.getTranslatedCaptionMsg(SYSDefaultLanguage,"GST") + " Amount (" + SYSCurrCom + ")");
            headerCell.setCellStyle(headerstyle);
            headerCell.setCellStyle(headerstyle);
            
            region = new Region(intTableMover, (short)columnCount, intTableMover, (short)columnCount);
            sheet.addMergedRegion(region);
            setRegion((short)1, sheet, region, workBook);
            headerCell = null;
            headerCell = headerRowSub.createCell((short)(columnCount++));
            headerCell.setCellValue("GRN Amount Incl " + adlangmstSQL.getTranslatedCaptionMsg(SYSDefaultLanguage,"GST") + " (" + SYSCurrCom + ")");
            headerCell.setCellStyle(headerstyle);
        }
 
        if(taxRptFormat.equals("1")) {
            region = new Region(intTableMover, (short)columnCount, intTableMover, (short)(columnCount + 6));
        } else {
            region = new Region(intTableMover, (short)columnCount, intTableMover, (short)(columnCount + 3));
        }
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        headerCell = null;
        headerCell = headerRow.createCell((short)columnCount);
        headerCell.setCellValue("FOR AEON CREDIT NOTE TO SUPPLIER");
        headerCell.setCellStyle(headerstyle);
        
        region = new Region(intTableMover, (short)columnCount, intTableMover, (short)columnCount);
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        headerCell = null;
        headerCell = headerRowSub.createCell((short)(columnCount++));
        headerCell.setCellValue("Credit Note Number");
        headerCell.setCellStyle(headerstyle);
        
        region = new Region(intTableMover, (short)columnCount, intTableMover, (short)columnCount);
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        headerCell = null;
        headerCell = headerRowSub.createCell((short)(columnCount++));
        headerCell.setCellValue("Margin (%)");
        headerCell.setCellStyle(headerstyle);
        
        region = new Region(intTableMover, (short)columnCount, intTableMover, (short)columnCount);
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        headerCell = null;
        headerCell = headerRowSub.createCell((short)(columnCount++));
        headerCell.setCellValue(strCreditCostAmount);
        headerCell.setCellStyle(headerstyle);
        
        region = new Region(intTableMover, (short)columnCount, intTableMover, (short)columnCount);
        sheet.addMergedRegion(region);
        setRegion((short)1, sheet, region, workBook);
        headerCell = null;
        headerCell = headerRowSub.createCell((short)(columnCount++));
        headerCell.setCellValue(strCreditCommAmount);
        headerCell.setCellStyle(headerstyle);
        headerCell.setCellStyle(headerstyle);
        
        if(taxRptFormat.equals("1")) {
            region = new Region(intTableMover, (short)columnCount, intTableMover, (short)columnCount);
            sheet.addMergedRegion(region);
            setRegion((short)1, sheet, region, workBook);
            headerCell = null;
            headerCell = headerRowSub.createCell((short)(columnCount++));
            headerCell.setCellValue("Commission Tax Rate (%)");
            headerCell.setCellStyle(headerstyle);
            headerCell.setCellStyle(headerstyle);
            
            region = new Region(intTableMover, (short)columnCount, intTableMover, (short)columnCount);
            sheet.addMergedRegion(region);
            setRegion((short)1, sheet, region, workBook);
            headerCell = null;
            headerCell = headerRowSub.createCell((short)(columnCount++));
            headerCell.setCellValue(adlangmstSQL.getTranslatedCaptionMsg(SYSDefaultLanguage,"GST") + " Amount (" + SYSCurrCom + ")");
            headerCell.setCellStyle(headerstyle);
            headerCell.setCellStyle(headerstyle);
            
            region = new Region(intTableMover, (short)columnCount, intTableMover, (short)columnCount);
            sheet.addMergedRegion(region);
            setRegion((short)1, sheet, region, workBook);
            headerCell = null;
            headerCell = headerRowSub.createCell((short)(columnCount++));
            headerCell.setCellValue("Commission Amount Incl. " + adlangmstSQL.getTranslatedCaptionMsg(SYSDefaultLanguage,"GST") + " (" + SYSCurrCom + ")");
            headerCell.setCellStyle(headerstyle);
        }
    }

    private void createDataConcess(HSSFRow headerRow, ResultSet result)
        throws Exception
    {
        Exception exception;
        int i = 0;
        String strDocumentID = "";
        String strStoreCode = "";
        String strStoreName = "";
        String strItemCode = "";
        String strItemDesc = "";
        String strClassCode = "";
        String strClassDesc = "";
        String strCategoryCode = "";
        String strType = "";
        String strINV_NO = "";
        double dblQuantity = 0.0D;
        double dblSellWovat = 0.0D;
        double dblSellVatRate = 0.0D;
        double dblSellVat = 0.0D;
        double dblMargin = 0.0D;
        double dblCostAmount = 0.0D;
        double dblCommWovat = 0.0D;
        double dblCommVatRate = 0.0D;
        double dblCommVat = 0.0D;
        double dblCommWvat = 0.0D;
        double dblCostWovat = 0.0D;
        double dblCostVatRate = 0.0D;
        double dblCostVat = 0.0D;
        double dblCostWvat = 0.0D;
        String dblDN_INVOICE_NO = "";
        double dblDN_COST_WOVAT = 0.0D;
        double dblDN_RATE = 0.0D;
        double dblDN_VAT_AMT = 0.0D;
        double dblDN_COST_WVAT = 0.0D;
        String dblCN_INVOICE_NO = "";
        double dblCN_MARGIN = 0.0D;
        double dblCN_COST_WOVAT = 0.0D;
        double dblCN_CMM_COST_WOVAT = 0.0D;
        double dblCN_CMM_RATE = 0.0D;
        double dblCN_CMM_VAT_AMT = 0.0D;
        double dblCN_CMM_COST_WVAT = 0.0D;
        HSSFCellStyle localStrcellStyle;
        HSSFCellStyle localAmountcellStyle;
        
        try
        {
            AltDescUtil.getDesc(USER_LANGUAGE, resultSet.getString("ITEM_DESC"));
            strDocumentID = resultSet.getString("DOCUMENT_NO");
            strStoreCode = resultSet.getString("STORE");
            strStoreName = AltDescUtil.getDesc(USER_LANGUAGE, resultSet.getString("STORE_NAME"));
            strItemCode = resultSet.getString("SHORT_SKU");
            
            if(strItemCode == null || strItemCode.equals("XX"))
            {
              strItemCode = "";
            }
            
            strItemDesc = AltDescUtil.getDesc(USER_LANGUAGE, resultSet.getString("ITEM_DESC"));
            strClassCode = resultSet.getString("DEPT");
            strClassDesc = resultSet.getString("CLASS_DESC");
            strCategoryCode = resultSet.getString("SUBCLASS");
            strType = resultSet.getString("TYPE");
            dblQuantity = resultSet.getDouble("QTY");
            dblSellWovat = resultSet.getDouble("EXT_SELL_WOVAT");
            dblSellVatRate = resultSet.getDouble("SVAT_RATE");
            dblSellVat = resultSet.getDouble("VAT_AMT");
            //FOR AEON TAX INVOICE TO SUPPLIER
            //System.out.println("FORM : " + resultSet.getString("FORM") + " SERIAL : " + resultSet.getString("SERIAL_NO") + " INVOICE_NO : " + resultSet.getString("INVOICE_NO"));
            if (resultSet.getString("INVOICE_NO") != null && !resultSet.getString("INVOICE_NO").equals(""))
            {
                if (!SYSGenInvNo.equals("2"))
                    strINV_NO = resultSet.getString("INVOICE_NO");
                else
                    strINV_NO = formInvoiceNo(resultSet.getString("FORM"), resultSet.getString("SERIAL_NO"), resultSet.getString("INVOICE_NO"));
            }
            else 
                strINV_NO = "";
            dblMargin = resultSet.getDouble("MARGIN");
            dblCostAmount = resultSet.getDouble("TI_COST_WOVAT");
            dblCommWovat = resultSet.getDouble("TI_COMM_COST_WOVAT");
            dblCommVatRate = resultSet.getDouble("TI_COMM_RATE");
            dblCommVat = resultSet.getDouble("TI_COMM_VAT_AMT");
            dblCommWvat = resultSet.getDouble("TI_COMM_COST_WVAT");
            //FOR SUPPLIER TAX INVOICE TO AEON
            dblCostWovat = resultSet.getDouble("SUPP_COST_WOVAT");
            dblCostVatRate = resultSet.getDouble("SUPPL_VAT_RATE");
            dblCostVat = resultSet.getDouble("SUPPL_VAT_AMT");
            dblCostWvat = resultSet.getDouble("SUPP_COST_WVAT");
            //System.out.println("FORM : " + resultSet.getString("DN_FORM") + " SERIAL : " + resultSet.getString("DN_SERIAL_NO") + " INVOICE_NO : " + resultSet.getString("DN_INVOICE_NO"));
            //FOR AEON DEBIT NOTE TO SUPPLIER                
            if (resultSet.getString("DN_INVOICE_NO") != null && !resultSet.getString("DN_INVOICE_NO").equals(""))
            {
                if (!SYSGenInvNo.equals("2"))
                    dblDN_INVOICE_NO = resultSet.getString("DN_INVOICE_NO");
                else
                    dblDN_INVOICE_NO = formInvoiceNo(resultSet.getString("DN_FORM"), resultSet.getString("DN_SERIAL_NO"), resultSet.getString("DN_INVOICE_NO"));            

            }
            else 
                dblDN_INVOICE_NO = "";                    
            dblDN_COST_WOVAT = resultSet.getDouble("DN_COST_WOVAT");
            dblDN_RATE = resultSet.getDouble("DN_RATE");
            dblDN_VAT_AMT = resultSet.getDouble("DN_VAT_AMT");
            dblDN_COST_WVAT = resultSet.getDouble("DN_COST_WVAT");
            //FOR AEON CREDIT NOTE TO SUPPLIER
            dblCN_INVOICE_NO = resultSet.getString("CN_INVOICE_NO");            
            dblCN_MARGIN = resultSet.getDouble("CN_MARGIN");
            dblCN_COST_WOVAT = resultSet.getDouble("CN_COST_WOVAT");
            dblCN_CMM_COST_WOVAT = resultSet.getDouble("CN_CMM_COST_WOVAT");
            dblCN_CMM_RATE = resultSet.getDouble("CN_CMM_RATE");
            dblCN_CMM_VAT_AMT = resultSet.getDouble("CN_CMM_VAT_AMT");
            dblCN_CMM_COST_WVAT = resultSet.getDouble("CN_CMM_COST_WVAT");
            
            localStrcellStyle =  cellstyle;
            localAmountcellStyle = cellAmountstyle;
              
            if(strType.equals("DOCUMENT TOTAL") || strType.equals("GRAND TOTAL"))
            {
              strDocumentID = "";
              strStoreCode = "";
              strStoreName = "";
              strItemCode = "";
              strItemDesc = "";
              strClassCode = "";
              strClassDesc = strType;
              strType = "";
              localStrcellStyle =  cellGrdTtlstyle;
              localAmountcellStyle = cellTtlAmountstyle;
              
            }
//            dblCostAmount = resultSet.getDouble("ITEM_COST") - dblCommWovat;
            dblCommVat = dblCommWvat - dblCommWovat;
            dblTotSellWovat += dblSellWovat;
            dblTotSellVat += dblSellVat;
            dblTotCostAmount += dblCostAmount;
            dblTotCommWovat += dblCommWovat;
            dblTotCommVat += dblCommVat;
            dblTotCommWvat += dblCommWvat;
            dblTotCostWovat += dblCostWovat;
            dblTotCostVat += dblCostVat;
            dblTotCostWvat += dblCostWvat;
            dblGrdTotSellWovat += dblSellWovat;
            dblGrdTotSellVat += dblSellVat;
            dblGrdTotCostAmount += dblCostAmount;
            dblGrdTotCommWovat += dblCommWovat;
            dblGrdTotCommVat += dblCommVat;
            dblGrdTotCommWvat += dblCommWvat;
            dblGrdTotCostWovat += dblCostWovat;
            dblGrdTotCostVat += dblCostVat;
            dblGrdTotCostWvat += dblCostWvat;
            headerCell = null;
            headerCell = headerRow.createCell((short)(i++));
            headerCell.setCellValue(strDocumentID);
            headerCell.setCellStyle(cellstyle);
            headerCell = null;
            headerCell = headerRow.createCell((short)(i++));
            headerCell.setCellValue(strStoreCode);
            headerCell.setCellStyle(cellstyle);
            
            headerCell = null;
            headerCell = headerRow.createCell((short)(i++));
            headerCell.setCellValue(getDescription(strStoreName));
            headerCell.setCellStyle(cellstyle);
            
            headerCell = null;
            headerCell = headerRow.createCell((short)(i++));
            headerCell.setCellValue(strItemCode);
            headerCell.setCellStyle(cellstyle);
            
            headerCell = null;
            headerCell = headerRow.createCell((short)(i++));
            headerCell.setCellValue(getDescription(strItemDesc));
            headerCell.setCellStyle(cellstyle);
            
            headerCell = null;
            headerCell = headerRow.createCell((short)(i++));
            headerCell.setCellValue(strClassCode);
            headerCell.setCellStyle(cellstyle);
            
            headerCell = null;
            headerCell = headerRow.createCell((short)(i++));
            headerCell.setCellValue(getDescription(strClassDesc));
            headerCell.setCellStyle(localStrcellStyle);
            
            headerCell = null;
            headerCell = headerRow.createCell((short)(i++));
            headerCell.setCellValue(strCategoryCode);
            headerCell.setCellStyle(cellstyle);
            
            headerCell = null;
            headerCell = headerRow.createCell((short)(i++));
            headerCell.setCellValue(strType);
            headerCell.setCellStyle(cellstyle);
            
            headerCell = null;
            headerCell = headerRow.createCell((short)(i++));
            if(resultSet.getString("TYPE").equals("DOCUMENT TOTAL") || resultSet.getString("TYPE").equals("GRAND TOTAL"))
            {
              headerCell.setCellValue(" ");
            }
            else
            {
              headerCell.setCellValue(dblQuantity);
            }
            headerCell.setCellStyle(localAmountcellStyle);
            headerCell = null;
            headerCell = headerRow.createCell((short)(i++));
            headerCell.setCellValue(dblSellWovat);
            headerCell.setCellStyle(localAmountcellStyle);
            
            if (taxRptFormat.equals("1")) {
                headerCell = null;
                headerCell = headerRow.createCell((short) (i++));
                if (resultSet.getString("TYPE").equals("DOCUMENT TOTAL") || resultSet.getString("TYPE").equals("GRAND TOTAL")) {
                    headerCell.setCellValue(" ");
                } else {
                    headerCell.setCellValue(dblSellVatRate);
                }
                headerCell.setCellStyle(localAmountcellStyle);

                headerCell = null;
                headerCell = headerRow.createCell((short) (i++));
                headerCell.setCellValue(dblSellVat);
                headerCell.setCellStyle(localAmountcellStyle);
            }
            
            
            headerCell = null;
            //FOR AEON TAX INVOICE TO SUPPLIER
            headerCell = headerRow.createCell((short)(i++));
            headerCell.setCellValue(strINV_NO);
            headerCell.setCellStyle(cellstyle);
            
            headerCell = null;
            headerCell = headerRow.createCell((short)(i++));
            if(resultSet.getString("TYPE").equals("DOCUMENT TOTAL") || resultSet.getString("TYPE").equals("GRAND TOTAL"))
            {
              headerCell.setCellValue(" ");
            }
            else
            {
              headerCell.setCellValue(dblMargin);
            }
            headerCell.setCellStyle(localAmountcellStyle);
            
            headerCell = null;
            headerCell = headerRow.createCell((short)(i++));
            headerCell.setCellValue(dblCostAmount);
            headerCell.setCellStyle(localAmountcellStyle);
            
            headerCell = null;
            headerCell = headerRow.createCell((short)(i++));
            headerCell.setCellValue(dblCommWovat);
            headerCell.setCellStyle(localAmountcellStyle);
            
            if (taxRptFormat.equals("1")) {
                headerCell = null;
                headerCell = headerRow.createCell((short) (i++));
                if (resultSet.getString("TYPE").equals("DOCUMENT TOTAL") || resultSet.getString("TYPE").equals("GRAND TOTAL")) {
                    headerCell.setCellValue(" ");
                } else {
                    headerCell.setCellValue(dblCommVatRate);
                }
                headerCell.setCellStyle(localAmountcellStyle);
                
                headerCell = null;
                headerCell = headerRow.createCell((short) (i++));
                headerCell.setCellValue(dblCommVat);
                headerCell.setCellStyle(localAmountcellStyle);
                
                headerCell = null;
                headerCell = headerRow.createCell((short) (i++));
                headerCell.setCellValue(dblCommWvat);
                headerCell.setCellStyle(localAmountcellStyle);
            }
            
            //FOR SUPPLIER TAX INVOICE TO AEON
            headerCell = null;
            headerCell = headerRow.createCell((short)(i++));
            headerCell.setCellValue(dblCostWovat);
            headerCell.setCellStyle(localAmountcellStyle);
            
            if (taxRptFormat.equals("1")) {
                headerCell = null;
                headerCell = headerRow.createCell((short)(i++));
                if(resultSet.getString("TYPE").equals("DOCUMENT TOTAL") || resultSet.getString("TYPE").equals("GRAND TOTAL")) {
                  headerCell.setCellValue(" ");
                }
                else {
                  headerCell.setCellValue(dblCostVatRate);
                }
                headerCell.setCellStyle(localAmountcellStyle);
                
                headerCell = null;
                headerCell = headerRow.createCell((short)(i++));
                headerCell.setCellValue(dblCostVat);
                headerCell.setCellStyle(localAmountcellStyle);
                
                headerCell = null;
                headerCell = headerRow.createCell((short)(i++));
                headerCell.setCellValue(dblCostWvat);
                headerCell.setCellStyle(localAmountcellStyle);
            }
            
            //FOR AEON DEBIT NOTE TO SUPPLIER
            headerCell = null;
            headerCell = headerRow.createCell((short)(i++));
            headerCell.setCellValue(dblDN_INVOICE_NO);
            headerCell.setCellStyle(cellstyle);
            
            headerCell = null;
            headerCell = headerRow.createCell((short)(i++));
            headerCell.setCellValue(dblDN_COST_WOVAT);
            headerCell.setCellStyle(localAmountcellStyle);
            
            if (taxRptFormat.equals("1")) {
                headerCell = null;
                headerCell = headerRow.createCell((short)(i++));
                if(resultSet.getString("TYPE").equals("DOCUMENT TOTAL") || resultSet.getString("TYPE").equals("GRAND TOTAL")) {
                  headerCell.setCellValue(" ");
                }
                else {
                  headerCell.setCellValue(dblDN_RATE);
                }
                headerCell.setCellStyle(localAmountcellStyle);

                headerCell = null;
                headerCell = headerRow.createCell((short)(i++));
                headerCell.setCellValue(dblDN_VAT_AMT);
                headerCell.setCellStyle(localAmountcellStyle);
                
                headerCell = null;
                headerCell = headerRow.createCell((short)(i++));
                headerCell.setCellValue(dblDN_COST_WVAT);
                headerCell.setCellStyle(localAmountcellStyle);
            }
            
            
            //FOR AEON CREDIT NOTE TO SUPPLIER
            headerCell = null;
            headerCell = headerRow.createCell((short)(i++));
            headerCell.setCellValue(dblCN_INVOICE_NO);
            headerCell.setCellStyle(cellstyle);
            
            headerCell = null;
            headerCell = headerRow.createCell((short)(i++));
            if(resultSet.getString("TYPE").equals("DOCUMENT TOTAL") || resultSet.getString("TYPE").equals("GRAND TOTAL"))
            {
              headerCell.setCellValue(" ");
            }
            else
            {
              headerCell.setCellValue(dblCN_MARGIN);
            }
            headerCell.setCellStyle(localAmountcellStyle);
            
            headerCell = null;
            headerCell = headerRow.createCell((short)(i++));
            headerCell.setCellValue(dblCN_COST_WOVAT);
            headerCell.setCellStyle(localAmountcellStyle);
            
            headerCell = null;
            headerCell = headerRow.createCell((short)(i++));
            headerCell.setCellValue(dblCN_CMM_COST_WOVAT);
            headerCell.setCellStyle(localAmountcellStyle);
            
            if (taxRptFormat.equals("1")) {
                headerCell = null;
                headerCell = headerRow.createCell((short)(i++));
                if(resultSet.getString("TYPE").equals("DOCUMENT TOTAL") || resultSet.getString("TYPE").equals("GRAND TOTAL")) {
                  headerCell.setCellValue(" ");
                }
                else {
                  headerCell.setCellValue(dblCN_CMM_RATE);
                }
                headerCell.setCellStyle(localAmountcellStyle);
                
                headerCell = null;
                headerCell = headerRow.createCell((short)(i++));
                headerCell.setCellValue(dblCN_CMM_VAT_AMT);
                headerCell.setCellStyle(localAmountcellStyle);
                
                headerCell = null;
                headerCell = headerRow.createCell((short)(i++));
                headerCell.setCellValue(dblCN_CMM_COST_WVAT);
                headerCell.setCellStyle(localAmountcellStyle);
            }
            

        }
        catch(Exception e)
        {
            throw e;
        }
    }


    private void createEmptyRowConcess(int thisRow, HSSFRow headerRow, ResultSet result)
        throws Exception
    {
        int i = 0;
        int totalColOfReport = intTotalColumn;
        if(!taxRptFormat.equals("1")) {
            totalColOfReport = totalColOfReport - 14;
        }
        try
        {
            headerRow = null;
            headerRow = sheet.createRow((short)thisRow);
            for(i = 0; i < totalColOfReport; i++)
            {
                headerCell = null;
                headerCell = headerRow.createCell((short)i);
                headerCell.setCellValue("");
                headerCell.setCellStyle(cellstyle);
            }

        }
        catch(Exception e)
        {
            throw e;
        }
    }

    private void printNoData()
        throws Exception
    {
        region = new Region(intTitleMover + 2, (short)0, intTitleMover + 2, (short)2);
        sheet.addMergedRegion(region);
        headerRow = sheet.createRow((short)intTitleMover + 2);
        headerCell = headerRow.createCell((short)0);
        headerCell.setCellValue("NO DATA TO PRINT");
        headerCell.setCellStyle(superheaderstyle);
    }

    private String getProfitvvValue(String vnm)
        throws Exception
    {
        profitvvSQL.setVObject((new ProfitvvSQL()).getVObject());
        profitvvSQL.setCOY(USER_COY);
        profitvvSQL.setVNM(vnm);
        profitvvSQL.getByKey();
        return profitvvSQL.VNM_VDTVL();
    }

    private void getCOYMST(String strCoy)
        throws Exception
    {
        coymstSQL.setCOY(strCoy);
        coymstSQL.getByKey();
    }

    private void getCOYSUBMST(String strCoy, String strCoySub)
        throws Exception
    {
        coysubmstSQL.setCOY(strCoy);
        coysubmstSQL.setCOY_SUB(strCoySub);
        coysubmstSQL.getByKey();
    }

    private void getVNMSTR(String strVnCode)
        throws Exception
    {
        vnmstrSQL.setVN_CODE(strVnCode);
        vnmstrSQL.getByKey();
    }
    
  private String formInvoiceNo(String form, String serial, String invoiceNo)
    throws SQLException, Exception
  {   String apInvoice = "";
      String formula[] = SYSVRInvFormula.split("");
      //System.out.println("FORM : " + form + " SERIAL : " + serial + " INVOICE_NO : " + invoiceNo);    
      for(int i = 1; i < formula.length; i++)
      {
          if(formula[i].equals("F"))
          {
              apInvoice = apInvoice + form;
              continue;
          }
          
          if(formula[i].equals("S"))
          {
              apInvoice = apInvoice + serial;
              continue;
          }
          
          if(formula[i].equals("I"))
              apInvoice = apInvoice + invoiceNo;
          else
          {
              if(!apInvoice.trim().equalsIgnoreCase(""))
                  apInvoice = apInvoice + formula[i];
          }    
      }
    
    System.out.println("apInvoice : " + apInvoice);
    
    return apInvoice;
  }

    private void closeStatement()
    {
        try
        {
            if(workBook != null)
                workBook = null;
        }
        catch(Exception e) { }
        try
        {
            if(resultSet != null)
            {
                resultSet.close();
                resultSet = null;
            }
        }
        catch(Exception Ex) { }
        try
        {
            if(pstmt != null)
                pstmt.close();
        }
        catch(Exception e) { }
        pstmt = null;
        try
        {
            if(psCmhdr != null)
                psCmhdr.close();
        }
        catch(Exception e) { }
        psCmhdr = null;
    }

    private void setSuperHeaderStyle()
        throws Exception
    {
        superheaderstyle = workBook.createCellStyle();
        HSSFFont font = workBook.createFont();
        font.setColor((short)32767);
        font.setBoldweight((short)700);
        superheaderstyle.setFont(font);
        superheaderstyle.setAlignment((short)1);
        superheaderstyle.setVerticalAlignment((short)1);
        superheaderstyle.setWrapText(true);
    }

    private void setCenterHeaderStyle()
        throws Exception
    {
        centerheaderstyle = workBook.createCellStyle();
        HSSFFont font = workBook.createFont();
        font.setColor((short)32767);
        font.setBoldweight((short)700);
        font.setFontHeight((short)intTitleShort);
        centerheaderstyle.setFont(font);
        centerheaderstyle.setAlignment((short)2);
        centerheaderstyle.setVerticalAlignment((short)1);
        centerheaderstyle.setWrapText(true);
    }

    private void setHeaderStyle()
        throws Exception
    {
        headerstyle = workBook.createCellStyle();
        HSSFFont font = workBook.createFont();
        font.setColor((short)32767);
        font.setBoldweight((short)700);
        headerstyle.setFont(font);
        headerstyle.setAlignment((short)2);
        headerstyle.setVerticalAlignment((short)0);
        headerstyle.setWrapText(true);
        headerstyle.setLeftBorderColor((short)8);
        headerstyle.setBorderLeft((short)1);
        headerstyle.setRightBorderColor((short)8);
        headerstyle.setBorderRight((short)1);
        headerstyle.setBorderBottom((short)1);
        headerstyle.setBottomBorderColor((short)8);
        headerstyle.setBorderTop((short)1);
        headerstyle.setTopBorderColor((short)8);
        headerstyle.setFillForegroundColor((short)31);
        headerstyle.setFillBackgroundColor((short)31);
        headerstyle.setFillPattern((short)1);
    }

    private void setForCellStyle()
        throws Exception
    {
        cellstyle = workBook.createCellStyle();
        HSSFFont font = workBook.createFont();
        font.setColor((short)32767);
        cellstyle.setFont(font);
        cellstyle.setAlignment((short)1);
        cellstyle.setVerticalAlignment((short)0);
        cellstyle.setWrapText(true);
        cellstyle.setLeftBorderColor((short)8);
        cellstyle.setBorderLeft((short)1);
        cellstyle.setRightBorderColor((short)8);
        cellstyle.setBorderRight((short)1);
        cellstyle.setBorderBottom((short)1);
        cellstyle.setBottomBorderColor((short)8);
        cellstyle.setBorderTop((short)1);
        cellstyle.setTopBorderColor((short)8);
        cellstyle.setFillForegroundColor((short)9);
    }

    private void setForCellAmountStyle()
        throws Exception
    {
        cellAmountstyle = workBook.createCellStyle();
        HSSFFont font = workBook.createFont();
        font.setColor((short)32767);
        cellAmountstyle.setFont(font);
        cellAmountstyle.setAlignment((short)3);
        cellAmountstyle.setVerticalAlignment((short)0);
        cellAmountstyle.setWrapText(true);
        cellAmountstyle.setLeftBorderColor((short)8);
        cellAmountstyle.setBorderLeft((short)1);
        cellAmountstyle.setRightBorderColor((short)8);
        cellAmountstyle.setBorderRight((short)1);
        cellAmountstyle.setBorderBottom((short)1);
        cellAmountstyle.setBottomBorderColor((short)8);
        cellAmountstyle.setBorderTop((short)1);
        cellAmountstyle.setTopBorderColor((short)8);
        cellAmountstyle.setFillForegroundColor((short)9);
        cellAmountstyle.setDataFormat(workBook.createDataFormat().getFormat("###,###,##0.00"));
    }

    private void setForCellTtlAmountStyle()
        throws Exception
    {
        cellTtlAmountstyle = workBook.createCellStyle();
        HSSFFont font = workBook.createFont();
        font.setColor((short)32767);
        font.setBoldweight((short)700);
        cellTtlAmountstyle.setFont(font);
        cellTtlAmountstyle.setAlignment((short)3);
        cellTtlAmountstyle.setVerticalAlignment((short)0);
        cellTtlAmountstyle.setWrapText(true);
        cellTtlAmountstyle.setLeftBorderColor((short)8);
        cellTtlAmountstyle.setBorderLeft((short)1);
        cellTtlAmountstyle.setRightBorderColor((short)8);
        cellTtlAmountstyle.setBorderRight((short)1);
        cellTtlAmountstyle.setBorderBottom((short)1);
        cellTtlAmountstyle.setBottomBorderColor((short)8);
        cellTtlAmountstyle.setBorderTop((short)1);
        cellTtlAmountstyle.setTopBorderColor((short)8);
        cellTtlAmountstyle.setFillForegroundColor((short)9);
        cellTtlAmountstyle.setDataFormat(workBook.createDataFormat().getFormat("###,###,##0.00"));
    }

    private void setForCellNumberStyle()
        throws Exception
    {
        cellNumberstyle = workBook.createCellStyle();
        HSSFFont font = workBook.createFont();
        font.setColor((short)32767);
        cellNumberstyle.setFont(font);
        cellNumberstyle.setAlignment((short)2);
        cellNumberstyle.setVerticalAlignment((short)0);
        cellNumberstyle.setWrapText(true);
        cellNumberstyle.setLeftBorderColor((short)8);
        cellNumberstyle.setBorderLeft((short)1);
        cellNumberstyle.setRightBorderColor((short)8);
        cellNumberstyle.setBorderRight((short)1);
        cellNumberstyle.setBorderBottom((short)1);
        cellNumberstyle.setBottomBorderColor((short)8);
        cellNumberstyle.setBorderTop((short)1);
        cellNumberstyle.setTopBorderColor((short)8);
        cellNumberstyle.setFillForegroundColor((short)9);
        cellNumberstyle.setDataFormat(workBook.createDataFormat().getFormat("###,###,##0"));
    }

    private void setForCellMarginStyle()
        throws Exception
    {
        cellMarginstyle = workBook.createCellStyle();
        HSSFFont font = workBook.createFont();
        font.setColor((short)32767);
        cellMarginstyle.setFont(font);
        cellMarginstyle.setAlignment((short)2);
        cellMarginstyle.setVerticalAlignment((short)0);
        cellMarginstyle.setWrapText(true);
        cellMarginstyle.setLeftBorderColor((short)8);
        cellMarginstyle.setBorderLeft((short)1);
        cellMarginstyle.setRightBorderColor((short)8);
        cellMarginstyle.setBorderRight((short)1);
        cellMarginstyle.setBorderBottom((short)1);
        cellMarginstyle.setBottomBorderColor((short)8);
        cellMarginstyle.setBorderTop((short)1);
        cellMarginstyle.setTopBorderColor((short)8);
        cellMarginstyle.setFillForegroundColor((short)9);
        cellMarginstyle.setDataFormat(workBook.createDataFormat().getFormat("###,###,##0.00"));
    }

    private void setForCellTotalStyle()
        throws Exception
    {
        cellTotalstyle = workBook.createCellStyle();
        HSSFFont font = workBook.createFont();
        font.setColor((short)32767);
        font.setBoldweight((short)700);
        cellTotalstyle.setFont(font);
        cellTotalstyle.setAlignment((short)3);
        cellTotalstyle.setVerticalAlignment((short)1);
        cellTotalstyle.setWrapText(true);
        cellTotalstyle.setLeftBorderColor((short)8);
        cellTotalstyle.setBorderLeft((short)1);
        cellTotalstyle.setRightBorderColor((short)8);
        cellTotalstyle.setBorderRight((short)1);
        cellTotalstyle.setBorderBottom((short)1);
        cellTotalstyle.setBottomBorderColor((short)8);
        cellTotalstyle.setBorderTop((short)1);
        cellTotalstyle.setTopBorderColor((short)8);
        cellTotalstyle.setFillForegroundColor((short)41);
        cellTotalstyle.setFillBackgroundColor((short)41);
        cellTotalstyle.setFillPattern((short)1);
        cellTotalstyle.setDataFormat(workBook.createDataFormat().getFormat("###,###,##0.00"));
    }

    private void setGrdTtlStyle()
        throws Exception
    {
        cellGrdTtlstyle = workBook.createCellStyle();
        HSSFFont font = workBook.createFont();
        font.setColor((short)32767);
        font.setBoldweight((short)700);
        cellGrdTtlstyle.setFont(font);
        cellGrdTtlstyle.setAlignment((short)1);
        cellGrdTtlstyle.setVerticalAlignment((short)0);
        cellGrdTtlstyle.setWrapText(true);
        cellGrdTtlstyle.setLeftBorderColor((short)8);
        cellGrdTtlstyle.setBorderLeft((short)1);
        cellGrdTtlstyle.setRightBorderColor((short)8);
        cellGrdTtlstyle.setBorderRight((short)1);
        cellGrdTtlstyle.setBorderBottom((short)1);
        cellGrdTtlstyle.setBottomBorderColor((short)8);
        cellGrdTtlstyle.setBorderTop((short)1);
        cellGrdTtlstyle.setTopBorderColor((short)8);
    }

    public static void main(String args[])
    {
        try
        {
            HParam hParam = new HParam();
            hParam.put("COY", "AMY");
            hParam.put("COY_SUB", "AMY");
            hParam.put("USER_ID", "OPR");
            hParam.put("SUPPL", "0000000001");
            hParam.put("SUPPL_CONT", "OS-001");
            hParam.put("FR_CONCESS_DATE", "2015-01-01");
            ConsSalesStmtRpt_2 consSalesStmtRpt_2 = new ConsSalesStmtRpt_2("C:\\Temp\\ConsSalesStmtRpt_"+qrMisc.getSqlSysDate()+".xls");
            consSalesStmtRpt_2.print(hParam);
        }
        catch(Exception de)
        {
            de.printStackTrace();
        }
        System.exit(0);
    }

    
}