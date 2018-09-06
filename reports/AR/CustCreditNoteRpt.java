package qrcom.PROFIT.reports.AR;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Vector;

import qrcom.PROFIT.files.dao.local.AR.CustCreditNoteRptDAO;
import qrcom.PROFIT.files.info.AdlangmstSQL;
import qrcom.PROFIT.files.info.CntxdetSQL;
import qrcom.PROFIT.files.info.CntxhdrInfo;
import qrcom.PROFIT.files.info.CntxhdrSQL;
import qrcom.PROFIT.files.info.CoymstSQL;
import qrcom.PROFIT.files.info.CoysubmstSQL;
import qrcom.PROFIT.files.info.CsinvhdrSQL;
import qrcom.PROFIT.files.info.ProfitvvSQL;
import qrcom.PROFIT.files.info.StrmstSQL;
import qrcom.PROFIT.files.info.SysctlSQL;
import qrcom.PROFIT.reports.GenericPageEvent;
import qrcom.PROFIT.reports.GenericReport;
import qrcom.PROFIT.shared.constants.IReport;
import qrcom.util.HParam;
import qrcom.util.ejb.jdbc.support.Parameters;
import qrcom.util.ejb.jdbc.support.StatementManager;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfWriter;

import java.sql.Date;

import qrcom.PROFIT.files.info.TaxrptctrlSQL;

public class CustCreditNoteRpt extends GenericReport {
    private Font FontHeaderType = null;
    private Font FontTitleType = null;
    private Font FontDetailType = null;
    private Font FontTableType = null;
    private Font FontDataType = null;
    private Font FontTotalType = null;
    private Font FontPagesType = null;

    private Table tableSuperHdr = null;
    private Table tableSuperSuperHdr = null;
    private Table tableHdr = null;
    private Table tableSuperTableHdr = null;
    private GenericPageEvent rpt_header_footer = null;

    private int intMaxRecord = 10;
    
    //private int tableHeaderwidths[] = {5, 10, 30, 6, 5, 7, 10, 10, 10, 10, 10};
    private int tableHeaderwidths[] = {3, 8, 31, 5, 5, 7, 9, 8, 8, 8, 8};

    private String strUSER_ID;
    private String strACTION;
    private String strCOY;
    private String strCOY_SUB;

    private String SYSCompanyLogo = "";
    private String SYSCompanyName = "";
    private String SYSCompanyRegNo = "";
    private String SYSReportLine = "";
    private String SYSRptFontTaxInv = "";
    private String SYSRptFontEncode2 = "";
    private String SYSCurrCom = "";
    private String SYSRptIdn = "";

    protected SimpleDateFormat strDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private CoymstSQL coymstSQL;
    private CoysubmstSQL coysubmstSQL;
    private CsinvhdrSQL csinvhdrSQL;
    private CntxhdrSQL cntxhdrSQL;
    private CntxdetSQL cntxdetSQL;
    private ProfitvvSQL profitvvSQL;
    private StrmstSQL strmstSQL;
    private SysctlSQL sysctlSQL;

    private StatementManager manager;
    private Vector<CntxhdrInfo> vctCntxhdr = new Vector<CntxhdrInfo>();

    private int intPageNum = 0;
    private int intPageTotal = 0;
    private int intCurrentItemNo = 0;
    private int intCurrentItemNoWOReset = 0;

    private TaxrptctrlSQL taxrptctrlSQL = null;
    private String taxRptFormat = "";
    private static final String DOC_TYPE = "Customer Credit Note";

	public CustCreditNoteRpt(String filename) {
		super(filename);
	}

	public CustCreditNoteRpt(OutputStream outStream) {
		super(outStream);
	}

	public void print(HParam hParam) {
		try {
			super.openOutputStream();
			manager = StatementManager.newInstance(conn);

			initObjSQL();
			jInit(hParam);

			boolean recordFound = false;

			Iterator<CntxhdrInfo> iter = vctCntxhdr.iterator();
			while (iter.hasNext()) {
				CntxhdrInfo cntxhdrInfo = (CntxhdrInfo) iter.next();
				printCreditNote(cntxhdrInfo);
				recordFound = true;
			}

			if (!recordFound) {
				printNoRecord();
			}

			conn.commit();
			document.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			manager.close(true);
			closeConnection();
			super.closeOutputStream();
		}
	}

    private void initObjSQL() throws SQLException {
        adlangmstSQL = new AdlangmstSQL(conn);
        coymstSQL = new CoymstSQL(conn);
        coysubmstSQL = new CoysubmstSQL(conn);
        csinvhdrSQL = new CsinvhdrSQL(conn);
        cntxhdrSQL = new CntxhdrSQL(conn);
        cntxdetSQL = new CntxdetSQL(conn);
        profitvvSQL = new ProfitvvSQL(conn);
        strmstSQL = new StrmstSQL(conn);
        sysctlSQL = new SysctlSQL(conn);
        taxrptctrlSQL = new TaxrptctrlSQL(conn);
    }

	private void jInit(HParam hParam) throws Exception {
		strUSER_ID = hParam.getString("USER_ID").trim();
		strACTION = isNotBlank(hParam.getString("PRINT_ACTION")) ? hParam.getString("PRINT_ACTION") : "PRINT_ALL";

		if (strACTION.equals("PRINT_ALL")) {
			CustCreditNoteRptDAO custCreditNoteRptDAO = new CustCreditNoteRptDAO(conn);
			vctCntxhdr = custCreditNoteRptDAO.getAllRecord(hParam);
		} else {
			String strTOTAL_ROW = isNotBlank(hParam.getString("TOTAL_ROW").trim()) ? hParam.getString("TOTAL_ROW").trim() : "0";
			int intTotalRow = Integer.parseInt(strTOTAL_ROW);
			for (int i = 1; i <= intTotalRow; i++) {
				String strSTORE = hParam.getString("STORE_" + String.valueOf(i)).trim();
				String strCRED_NO = hParam.getString("CRED_NO_" + String.valueOf(i)).trim();

				cntxhdrSQL.setVObject(new CntxhdrInfo().getVObject());
				cntxhdrSQL.setSTORE(strSTORE);
				cntxhdrSQL.setCRED_NO(Integer.parseInt(strCRED_NO));
				cntxhdrSQL.getByKey();

				CntxhdrInfo cntxhdrInfo = new CntxhdrInfo();
				cntxhdrInfo.setVObject(cntxhdrSQL.getVObject());
				vctCntxhdr.add(cntxhdrInfo);
			}
		}

		strCOY = retrieveUserCoy(strUSER_ID);
		strCOY_SUB = isNotBlank(hParam.getString("COY_SUB")) ? hParam.getString("COY_SUB") : "";
		USER_LANGUAGE = retrieveUserLanguage(strUSER_ID);

		setGroupingForReport(strCOY);

		if (isBlank(USER_LANGUAGE)) {
			USER_LANGUAGE = "0";
		}

		SYSCompanyLogo = getPROFITVV("SYSCompanyLogo");
		SYSCompanyName = getPROFITVV("SYSCompanyName");
		SYSCompanyRegNo = getPROFITVV("SYSCompanyRegNo");
		SYSReportLine = getPROFITVV("SYSReportLine");
		SYSRptFontTaxInv = getPROFITVV("SYSRptFontTaxInv");
		SYSRptFontEncode2 = getPROFITVV("SYSRptFontEncode2");
		SYSCurrCom = getPROFITVV("SYSCurrCom");
		SYSRptIdn = getPROFITVV("SYSRptIdn");

		BASE_FONT_Chinese = BaseFont.createFont(SYSRptFontTaxInv, SYSRptFontEncode2, BaseFont.NOT_EMBEDDED);

		FontChinese = new Font(BASE_FONT_Chinese, 10, Font.NORMAL);
		FontHeaderType = new Font(BASE_FONT_Chinese, 8, Font.NORMAL);
		FontTitleType = new Font(BASE_FONT_Chinese, 11.5f, Font.BOLD);
		FontDetailType = new Font(BASE_FONT_Chinese, 8, Font.NORMAL);
		FontTableType = new Font(BASE_FONT_Chinese, 7.5f, Font.BOLD);
		FontDataType = new Font(BASE_FONT_Chinese_sub, 7, Font.NORMAL);
		FontTotalType = new Font(BASE_FONT_Chinese, 7.5f, Font.BOLD);
		FontPagesType = new Font(BASE_FONT_Chinese, 8, Font.NORMAL);

		rpt_header_footer = new GenericPageEvent(conn, FontChinese);
		rpt_header_footer.setCoy(strCOY, USER_LANGUAGE, strUSER_ID);

		// Creation Of The Different Writers
		document = new Document(PageSize.A4); // Margin : left, right, top, bottom
		document.setMargins(30, 18, 45, 45);
		pdfwriter = PdfWriter.getInstance(document, outStream); // MUST Place This Line Right After The New Document(...
		// pdfwriter.setPageEvent(rpt_header_footer);

		document.open();
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

	private void getCSINVHDR(String strCoy, String strCoySub, String strInvNo) throws Exception {
		csinvhdrSQL.setCOY(strCoy);
		csinvhdrSQL.setCOY_SUB(strCoySub);
		csinvhdrSQL.setINVOICE_NO(strInvNo);
		csinvhdrSQL.getByKey();
	}

	private void resetValue() throws Exception {
		intPageNum = 1;
		intPageTotal = 1;
		intCurrentItemNo = 0;
		intCurrentItemNoWOReset = 0;
	}

    private void printCreditNote(CntxhdrInfo cntxhdrInfo) throws Exception {
        cntxhdrSQL.setVObject(cntxhdrInfo.getVObject());
        resetValue();

        ResultSet rsDet = null;
        Table datatable = null;
        Table blankTable = null;

        getCOYMST(cntxhdrInfo.COY());
        getCOYSUBMST(cntxhdrInfo.COY(), cntxhdrInfo.COY_SUB());
        getSTRMST(cntxhdrInfo.STORE());
        getCSINVHDR(cntxhdrInfo.COY(), cntxhdrInfo.COY_SUB(), cntxhdrInfo.CN_INVOICE_NO());

        String strCN_TYPE = cntxhdrInfo.CN_TYPE();

        getTaxRptFormat(cntxhdrInfo);

        printNewPage();
        initializeHeaderTable(strCN_TYPE);
        printHeader(tableSuperSuperHdr, tableSuperHdr, tableSuperTableHdr, tableHdr);

        if (strCN_TYPE.equals("Q")) {
            Parameters parameters = Parameters.builder(2).set(cntxhdrInfo.STORE()).set(cntxhdrInfo.CRED_NO()).build();
            rsDet = manager.select(getCntxdetQuery(""), parameters);
            int totalData = (int) manager.count(getCntxdetQuery("COUNT"), parameters);
            intPageTotal = (int) Math.ceil((double) totalData / intMaxRecord);

            if (intPageTotal < 1) {
                intPageTotal = 1;
            }

            if (!rsDet.isBeforeFirst()) {

            } else {
                while (rsDet != null && rsDet.next()) {
                    cntxdetSQL.populate(rsDet);

                    intCurrentItemNo++;
                    intCurrentItemNoWOReset++;

                    if (intCurrentItemNo > intMaxRecord) {
                        createBlankSummaryTable();
                        printTableFooter(strCN_TYPE);

                        intPageNum++;
                        printNewPage();
                        initializeHeaderTable(strCN_TYPE);
                        printHeader(tableSuperSuperHdr, tableSuperHdr, tableSuperTableHdr, tableHdr);
                        intCurrentItemNo = 1;
                    }

                    datatable = createDataTable(intCurrentItemNoWOReset);
                    document.add(datatable);
                }

                for (int i = intCurrentItemNo + 1; i <= intMaxRecord; i++) {
                    intCurrentItemNoWOReset++;
                    blankTable = createBlankTable(intCurrentItemNoWOReset);
                    document.add(blankTable);
                }

                createSummaryTable();
                printTableFooter(strCN_TYPE);
            }
        } else {
            intCurrentItemNo++;
            intCurrentItemNoWOReset++;

            datatable = createWOItemDataTable(intCurrentItemNoWOReset);
            document.add(datatable);

            createSummaryTable();
            printTableFooter(strCN_TYPE);
        }

        updateCNTXHDR(cntxhdrSQL);
    }
        
    private void getTaxRptFormat(CntxhdrInfo cntxhdrInfo) throws SQLException {
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
                    query = "SELECT " + strDateCtrl + " FROM CNTXHDR WHERE CRED_NO = ?";
                    parameters = Parameters.builder().add(cntxhdrInfo.CRED_NO());
                    ResultSet rs = manager.select(query, parameters);

                    if (rs != null && rs.next()) {
                        updatedDate = rs.getDate(strDateCtrl);
                        if (updatedDate == null) {
                            query = "SELECT DATE_RTN FROM CNTXHDR WHERE CRED_NO = ?";
                            parameters = Parameters.builder().add(cntxhdrInfo.CRED_NO());
                            rs = manager.select(query, parameters);
                            if (rs != null && rs.next()) {
                                updatedDate = rs.getDate("DATE_RTN");
                            }
                        }
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

	private void printNewPage() throws Exception {
		document.newPage();
	}

	private void printNoRecord() throws Exception {
		printNewPage();

		getCOYMST(strCOY);
		getCOYSUBMST(strCOY, strCOY_SUB);

		tableSuperSuperHdr = createTableSuperSuperHdr();
		tableSuperHdr = createTableSuperHdr();
		document.add(tableSuperSuperHdr);
		document.add(tableSuperHdr);

		Table table = new Table(3);

		int headerwidths[] = { 30, 40, 30 };

		table.setPadding(IReport.CELLPADDING);
		table.setSpacing(0);
		table.setWidths(headerwidths);
		table.setWidth(100);
		table.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
		table.setBorder(Rectangle.NO_BORDER);

		Cell cell = null;

		cell = new Cell(new Phrase("", FontTitleType));
		cell.disableBorderSide(Rectangle.BOX);
		table.addCell(cell);

		cell = new Cell(new Phrase(adlangmstSQL.getTranslatedCaptionMsg(USER_LANGUAGE, "No Data To Print"), FontChinese));
		cell.disableBorderSide(Rectangle.BOX);
		table.addCell(cell);

		cell = new Cell(new Phrase("", FontTitleType));
		cell.disableBorderSide(Rectangle.BOX);
		table.addCell(cell);

		document.add(table);
	}

	private void initializeHeaderTable(String strCN_TYPE) throws Exception {
		tableSuperSuperHdr = createTableSuperSuperHdr();
		tableSuperHdr = createTableSuperHdr();
		tableSuperTableHdr = createSuperTableHdr();
		if (strCN_TYPE.equals("Q")) {
			tableHdr = createTableHdr();
		} else {
			tableHdr = createWOItemTableHdr();
		}
	}

	private void printHeader(Table hdr1, Table hdr2, Table hdr3, Table hdr4)
			throws Exception {
		document.add(hdr1);
		document.add(hdr2);
		document.add(hdr3);

		printBlankCell(1);
		document.add(hdr4);
	}

    private Table createTableSuperSuperHdr() throws BadElementException,
                    DocumentException, SQLException, Exception {
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

        strCompName = SYSCompanyName + " " + SYSCompanyRegNo;
        strAddress1 = "Head Office: " + getDescription(coymstSQL.COY_ADDR1())
                        + " " + getDescription(coymstSQL.COY_ADDR2());
        strAddress2 = getDescription(coymstSQL.COY_ADDR3());
        strTelAndFax = "Tel: " + coymstSQL.COY_PHONE() + " Fax: " + coymstSQL.COY_FAX();
        
        if(taxRptFormat.equals("1")) {
            strGSTRegNo = adlangmstSQL.getTranslatedCaptionMsg(USER_LANGUAGE, "GST")
                            + " Reg. No.: " + coymstSQL.COY_TAX_REG_NO();
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

        if ((SYSRptIdn != "" || SYSRptIdn != null) && SYSRptIdn.equals("1")) {
        } else {
                cell = new Cell(new Phrase(strGSTRegNo, FontHeaderType)); // Company GST Registration Number
                cell.disableBorderSide(Rectangle.BOX);
                cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                tableSuperSuperHdr.addCell(cell);
        }
        return tableSuperSuperHdr;
    }

	private Table createTableSuperHdr() throws BadElementException,
			DocumentException, SQLException {
		Table tableSuperHdr = new Table(3);

		int headerwidths[] = { 30, 40, 30 };

		tableSuperHdr.setPadding(IReport.CELLPADDING);
		tableSuperHdr.setSpacing(0);
		tableSuperHdr.setWidths(headerwidths);
		tableSuperHdr.setWidth(100);
		tableSuperHdr.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
		tableSuperHdr.setBorder(Rectangle.NO_BORDER);

		Cell cell = null;

		cell = new Cell(new Phrase("\n", FontHeaderType));
		cell.disableBorderSide(Rectangle.BOX);
		cell.setColspan(3);
		tableSuperHdr.addCell(cell);

		cell = new Cell(new Phrase("", FontTitleType));
		cell.disableBorderSide(Rectangle.BOX);
		tableSuperHdr.addCell(cell);

		cell = new Cell(new Phrase("CREDIT NOTE", FontTitleType));
		cell.disableBorderSide(Rectangle.BOX);
		tableSuperHdr.addCell(cell);

		cell = new Cell(new Phrase("", FontTitleType));
		cell.disableBorderSide(Rectangle.BOX);
		tableSuperHdr.addCell(cell);

		return tableSuperHdr;
	}

	private Table createSuperTableHdr() throws BadElementException, DocumentException, SQLException, Exception {
        Table tableSuperTableHdr = new Table(5);

        int headerwidths[] = { 13, 40, 2, 25, 27 };

        tableSuperTableHdr.setPadding(0.5f);
        tableSuperTableHdr.setSpacing(0);
        tableSuperTableHdr.setWidths(headerwidths);
        tableSuperTableHdr.setWidth(100);
        tableSuperTableHdr.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
        tableSuperTableHdr.setBorder(Rectangle.NO_BORDER);

        String strBillTo = "";
        String strBillAddress = "";
        String strTel = "";
        String strGstRegNo;
        String strVatRegNo;
        String strShipTo = "";
        String strShipAddress = "";
        String strCNNo = "";
        String strCNDate = "";
        String strInvNo = "";
        String strInvDate = "";
        String strCreditTerm = "";
        String strStore = "";
        String strStoreName = "";

        strBillTo = csinvhdrSQL.BILL_CONTACT_NAME();
        strBillAddress = csinvhdrSQL.BILL_ADDRESS().trim();
        strTel = csinvhdrSQL.CS_PHONE();
        strShipTo = csinvhdrSQL.SHIP_CONTACT_NAME();
        strShipAddress = csinvhdrSQL.SHIP_ADDRESS().trim();

        strCNNo = cntxhdrSQL.CREDIT_NOTE_NO();
        if (cntxhdrSQL.CN_PRINTED_DATE() != null) {
            strCNNo += " (REPRINT)";
        }
        strCNDate = cntxhdrSQL.CREDIT_NOTE_DATE() == null ? "" : strDateFormat.format(cntxhdrSQL.CREDIT_NOTE_DATE());
        strInvNo = csinvhdrSQL.INVOICE_NO();
        strInvDate = csinvhdrSQL.INV_DATE() == null ? "" : strDateFormat.format(csinvhdrSQL.INV_DATE());
        strCreditTerm = getCreditTerm(cntxhdrSQL.CN_CUST_CODE(), cntxhdrSQL.CN_CUST_CONTRACT());
        strStore = strmstSQL.STORE();
        strStoreName = getDescription(strmstSQL.STORE_NAME());

        Cell cell = null;

        cell = new Cell(new Phrase("BILL TO:", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strBillTo, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setMaxLines(1);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("CREDIT NOTE NO.:", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strCNNo, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        // -----------------------------------------------------------------------------------------------//

        cell = new Cell(new Phrase("ADDRESS:", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strBillAddress, FontDetailType));
        cell.setRowspan(3);
        cell.setMaxLines(3);
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("CREDIT NOTE DATE:", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strCNDate, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        // -----------------------------------------------------------------------------------------------//

        cell = new Cell(new Phrase("", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("REFERENCE INVOICE NO.:", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strInvNo, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        // -----------------------------------------------------------------------------------------------//

        cell = new Cell(new Phrase("", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("REFERENCE INVOICE DATE.:", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strInvDate, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        // -----------------------------------------------------------------------------------------------//

        cell = new Cell(new Phrase("TEL:", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strTel, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("CREDIT TERM:", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strCreditTerm, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        // -----------------------------------------------------------------------------------------------//

        if (taxRptFormat.equals("1")) {
            strGstRegNo = adlangmstSQL.getTranslatedCaptionMsg(USER_LANGUAGE, "GST") + " REG. NO.:";
            strVatRegNo = csinvhdrSQL.VAT_REG_NO();
        } else {
            strGstRegNo = "\n";
            strVatRegNo = "\n";
        }
        cell = new Cell(new Phrase(strGstRegNo, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strVatRegNo, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("STORE CODE:", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strStore, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        // -----------------------------------------------------------------------------------------------//

        cell = new Cell(new Phrase("SHIP TO:", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strShipTo, FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setMaxLines(1);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase("STORE NAME:", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strStoreName, FontDetailType));
        cell.setRowspan(3);
        cell.setMaxLines(3);
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        // -----------------------------------------------------------------------------------------------//

        cell = new Cell(new Phrase("ADDRESS:", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        tableSuperTableHdr.addCell(cell);

        cell = new Cell(new Phrase(strShipAddress, FontDetailType));
        cell.setRowspan(3);
        cell.setMaxLines(3);
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

        // -----------------------------------------------------------------------------------------------//

        cell = new Cell(new Phrase("\n", FontDetailType));
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

        // -----------------------------------------------------------------------------------------------//

        cell = new Cell(new Phrase("\n", FontDetailType));
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

        // -----------------------------------------------------------------------------------------------//

        cell = new Cell(new Phrase("", FontDetailType));
        cell.disableBorderSide(Rectangle.BOX);
        tableSuperTableHdr.addCell(cell);
        cell.setColspan(5);

        return tableSuperTableHdr;
    }

	private String getCreditTerm(String strCustCode, String strCustContract)
			throws Exception {
		String strVN_CREDIT_DESCRIPTION = "";

		String query = " SELECT V.VN_CREDIT_DESCRIPTION FROM CSCNTERM C, VNTERMS V "
				+ " WHERE C.CS_DEF_TERMS = V.VN_CREDIT_TERMS_CODE "
				+ " AND C.CUST_CODE = ? "
				+ " AND C.CUST_CONTRACT = ? ";

		Parameters parameters = Parameters.builder(2)
				.set(strCustCode).set(strCustContract)
				.build();

		ResultSet rs = manager.select(query, parameters);

		if (rs != null && rs.next()) {
			strVN_CREDIT_DESCRIPTION = rs.getString(1);
		}

		return getDescription(strVN_CREDIT_DESCRIPTION);
	}

    private Table createTableHdr() throws BadElementException, DocumentException, SQLException, Exception {
        Table tableHeader = new Table(11);

        tableHeader.setPadding(1);
        tableHeader.setSpacing(0);
        tableHeader.setWidths(tableHeaderwidths);
        tableHeader.setWidth(100);
        tableHeader.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
        tableHeader.setDefaultVerticalAlignment(Element.ALIGN_MIDDLE);
        tableHeader.setBorder(Rectangle.NO_BORDER);
        tableHeader.setSpaceInsideCell(1.5f);

        Cell cell = null;

        cell = new Cell(new Phrase("NO.", FontTableType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setUseAscender(true);
        tableHeader.addCell(cell);

        cell = new Cell(new Phrase("ITEM CODE", FontTableType));
        cell.setUseAscender(true);
        tableHeader.addCell(cell);

        cell = new Cell(new Phrase("ITEM DESCRIPTION", FontTableType));
        cell.setUseAscender(true);
        tableHeader.addCell(cell);

        String strTaxCode;
        String strSubTotal;
        String strDiscount;
        String strTotalGst;
        String strGst;
        String strTotalIncl;
        if (taxRptFormat.equals("1")) {
            strTaxCode = "TAX CODE";
            strSubTotal = "SUBTOTAL EXCL. " + adlangmstSQL.getTranslatedCaptionMsg(USER_LANGUAGE, "GST") + " (" + SYSCurrCom + ")";
            strDiscount = "DISCOUNT EXCL. " + adlangmstSQL.getTranslatedCaptionMsg(USER_LANGUAGE, "GST") + " (" + SYSCurrCom + ")";
            strTotalGst = "TOTAL EXCL. " + adlangmstSQL.getTranslatedCaptionMsg(USER_LANGUAGE, "GST") + " (" + SYSCurrCom + ")";
            strGst = adlangmstSQL.getTranslatedCaptionMsg(USER_LANGUAGE, "GST") + " @6% (" + SYSCurrCom + ")";
            strTotalIncl = "TOTAL INCL. " + adlangmstSQL.getTranslatedCaptionMsg(USER_LANGUAGE, "GST") + " (" + SYSCurrCom + ")";
        } else {
            strTaxCode = "\n";
            strSubTotal = "SUBTOTAL (" + SYSCurrCom + ")";
            strDiscount = "DISCOUNT (" + SYSCurrCom + ")";
            strTotalGst = "\n";
            strGst = "\n";
            strTotalIncl = "TOTAL (" + SYSCurrCom + ")";
        }
        cell = new Cell(new Phrase(strTaxCode, FontTableType));
        cell.setUseAscender(true);
        tableHeader.addCell(cell);

        cell = new Cell(new Phrase("QTY", FontTableType));
        cell.setUseAscender(true);
        tableHeader.addCell(cell);

        cell = new Cell(new Phrase("UNIT PRICE (" + SYSCurrCom + ")", FontTableType));
        cell.setUseAscender(true);
        tableHeader.addCell(cell);

        cell = new Cell(new Phrase(strSubTotal, FontTableType));
        cell.setUseAscender(true);
        tableHeader.addCell(cell);

        cell = new Cell(new Phrase(strDiscount, FontTableType));
        cell.setUseAscender(true);
        tableHeader.addCell(cell);

        cell = new Cell(new Phrase(strTotalGst, FontTableType));
        cell.setUseAscender(true);
        tableHeader.addCell(cell);

        cell = new Cell(new Phrase(strGst, FontTableType));
        cell.setUseAscender(true);
        tableHeader.addCell(cell);

        cell = new Cell(new Phrase(strTotalIncl, FontTableType));
        cell.setUseAscender(true);
        tableHeader.addCell(cell);

        return tableHeader;
    }

	private Table createWOItemTableHdr() throws BadElementException,
			DocumentException, SQLException, Exception {
		Table tableHeader = new Table(11);

		tableHeader.setPadding(1);
		tableHeader.setSpacing(0);
		tableHeader.setWidths(tableHeaderwidths);
		tableHeader.setWidth(100);
		tableHeader.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
		tableHeader.setBorder(Rectangle.NO_BORDER);

		Cell cell = null;

		cell = new Cell(new Phrase("NO.", FontTableType));
		cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
		cell.setGrayFill(IReport.STD_GRAY);
		tableHeader.addCell(cell);

		cell = new Cell(new Phrase("DESCRIPTION", FontTableType));
		cell.setColspan(9);
		cell.setGrayFill(IReport.STD_GRAY);
		tableHeader.addCell(cell);

		cell = new Cell(new Phrase("AMOUNT" + " (" + SYSCurrCom + ")", FontTableType));
		cell.setGrayFill(IReport.STD_GRAY);
		tableHeader.addCell(cell);

		return tableHeader;
	}
	
	private void printBlankCell(int max) throws Exception {
		Table tableBlank = new Table(1);

		tableBlank.setPadding(IReport.CELLPADDING);
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

	private String getCntxdetQuery(String strType) throws Exception {
		String query = "";
		if (strType.equals("COUNT")) {
			query = "SELECT COUNT(*) ";
		} else {
			query = "SELECT * ";
		}
		query += "FROM CNTXDET "
			+ "WHERE STORE = ? "
			+ "AND CRED_NO = ? "
			+ "ORDER BY SHORT_SKU ";
		return query;
	}

    private Table createDataTable(int count) throws Exception {
        Table datatable = new Table(11);

        datatable.setPadding(IReport.CELLPADDING);
        datatable.setSpacing(0);
        datatable.setWidths(tableHeaderwidths);
        datatable.setWidth(100);
        datatable.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
        datatable.setDefaultVerticalAlignment(Element.ALIGN_TOP);
        datatable.setBorder(Rectangle.NO_BORDER);
        datatable.setSpaceInsideCell(3);

        String strNumber = "";
        String strItemCode = "";
        String strItemDesc = "";
        String strQty = "";
        String strUnitSellWvat = "";
        String strExtAmtWovat = "";
        String strExtDiscAmtWovat = "";
        String strExtNetAmtWovat;
        String strSalesVatAmount;
        String strExtNetAmtWvat = "";

        strNumber = String.valueOf(count);
        strItemCode = cntxdetSQL.SHORT_SKU();
        strItemDesc = getItemDesc();
        String strTaxCode;
        strQty = currencyConverter.sformat(cntxdetSQL.QTY_RTN(), 0);
        strUnitSellWvat = currencyConverter.format(cntxdetSQL.CN_UNIT_SELL_WOVAT());
        strExtAmtWovat = currencyConverter.format(cntxdetSQL.EXT_SELL_RTN_WOVAT());
        strExtDiscAmtWovat = currencyConverter.format(cntxdetSQL.EXT_DISC_AMT_WOVAT());
        strExtNetAmtWvat = currencyConverter.format(cntxdetSQL.EXT_NET_SELL_RTN_WVAT());

        if(taxRptFormat.equals("1")) {
            strTaxCode = cntxdetSQL.SALES_VAT_CODE();
            strExtNetAmtWovat = currencyConverter.format(cntxdetSQL.EXT_NET_SELL_RTN_WOVAT());
            strSalesVatAmount = currencyConverter.format(cntxdetSQL.SALES_VAT_AMT());
        } else {
            strTaxCode = "\n";
            strExtNetAmtWovat = "\n";
            strSalesVatAmount = "\n";
        }

        Cell cell = null;

        strNumber += "\n\n";
        cell = new Cell(new Phrase(strNumber, FontDataType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setUseAscender(true);
        cell.setRowspan(2);
        datatable.addCell(cell);

        cell = new Cell(new Phrase(strItemCode, FontDataType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setUseAscender(true);
        cell.setRowspan(2);
        datatable.addCell(cell);

        cell = new Cell(new Phrase(strItemDesc, FontDataType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setUseAscender(true);
        cell.enableBorderSide(Rectangle.BOX);
        cell.setRowspan(2);
        cell.setMaxLines(2);
        datatable.addCell(cell);

        cell = new Cell(new Phrase(strTaxCode, FontDataType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setUseAscender(true);
        cell.setRowspan(2);
        datatable.addCell(cell);

        cell = new Cell(new Phrase(strQty, FontDataType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setUseAscender(true);
        cell.setRowspan(2);
        datatable.addCell(cell);

        cell = new Cell(new Phrase(strUnitSellWvat, FontDataType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setUseAscender(true);
        cell.setRowspan(2);
        datatable.addCell(cell);

        cell = new Cell(new Phrase(strExtAmtWovat, FontDataType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setUseAscender(true);
        cell.setRowspan(2);
        datatable.addCell(cell);

        cell = new Cell(new Phrase(strExtDiscAmtWovat, FontDataType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setUseAscender(true);
        cell.setRowspan(2);
        datatable.addCell(cell);

        cell = new Cell(new Phrase(strExtNetAmtWovat, FontDataType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setUseAscender(true);
        cell.setRowspan(2);
        datatable.addCell(cell);

        cell = new Cell(new Phrase(strSalesVatAmount, FontDataType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setUseAscender(true);
        cell.setRowspan(2);
        datatable.addCell(cell);

        cell = new Cell(new Phrase(strExtNetAmtWvat, FontDataType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setUseAscender(true);
        cell.setRowspan(2);
        datatable.addCell(cell);

        return datatable;
    }

	private String getItemDesc() throws Exception {
		String strItemDesc = "";
		String query = "SELECT CS_ITEM_DESC "
				+ "FROM CSITEMMST "
				+ "WHERE CATEGORY = ? "
				+ "AND SHORT_SKU = ? "
				+ "AND CUST_CODE = ? ";

		Parameters parameters = Parameters.builder(3)
					.set(strmstSQL.STORE_CATEGORY()).set(cntxdetSQL.SHORT_SKU()).set(cntxhdrSQL.CN_CUST_CODE())
					.build();

		ResultSet rs = manager.select(query, parameters);

		if (rs != null && rs.next()) {
			strItemDesc = rs.getString("CS_ITEM_DESC");
		}
		return getDescription(strItemDesc);
	}

	private Table createWOItemDataTable(int count) throws Exception {
		Table datatable = new Table(11);

		datatable.setPadding(IReport.CELLPADDING);
		datatable.setSpacing(0);
		datatable.setWidths(tableHeaderwidths);
		datatable.setWidth(100);
		datatable.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
		datatable.setDefaultVerticalAlignment(Element.ALIGN_TOP);
		datatable.setBorder(Rectangle.NO_BORDER);

		String strNumber = "";
		String strDesc = "";
		String strAmount = "";

		strNumber = String.valueOf(count);
		strDesc = cntxhdrSQL.CN_REMARK();
		strAmount = currencyConverter.format(cntxhdrSQL.SELL_RTN_WOVAT_TOT());

		Cell cell = null;

		cell = new Cell(new Phrase(strNumber, FontDataType));
		cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
		cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		cell.disableBorderSide(Rectangle.BOX);
	    cell.enableBorderSide(Rectangle.TOP);
	    cell.enableBorderSide(Rectangle.LEFT);
	    cell.enableBorderSide(Rectangle.RIGHT);
		datatable.addCell(cell);

		cell = new Cell(new Phrase(strDesc, FontDataType));
		cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		cell.setVerticalAlignment(PdfPCell.ALIGN_TOP);
		cell.disableBorderSide(Rectangle.BOX);
	    cell.enableBorderSide(Rectangle.TOP);
	    cell.enableBorderSide(Rectangle.BOTTOM);
	    cell.enableBorderSide(Rectangle.LEFT);
	    cell.enableBorderSide(Rectangle.RIGHT);
		cell.setColspan(9);
		cell.setRowspan(20);
		datatable.addCell(cell);

		cell = new Cell(new Phrase(strAmount, FontDataType));
		cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		cell.disableBorderSide(Rectangle.BOX);
	    cell.enableBorderSide(Rectangle.TOP);
	    cell.enableBorderSide(Rectangle.LEFT);
	    cell.enableBorderSide(Rectangle.RIGHT);
		datatable.addCell(cell);

		for (int i = 2; i < 20; i++) {
			cell = new Cell(new Phrase("\n", FontDataType));
			cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
			cell.disableBorderSide(Rectangle.BOX);
		    cell.enableBorderSide(Rectangle.LEFT);
		    cell.enableBorderSide(Rectangle.RIGHT);
			datatable.addCell(cell);

			cell = new Cell(new Phrase("\n", FontDataType));
			cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
			cell.disableBorderSide(Rectangle.BOX);
		    cell.enableBorderSide(Rectangle.LEFT);
		    cell.enableBorderSide(Rectangle.RIGHT);
			datatable.addCell(cell);
		}

		cell = new Cell(new Phrase("\n", FontDataType));
		cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		cell.disableBorderSide(Rectangle.BOX);
	    cell.enableBorderSide(Rectangle.BOTTOM);
	    cell.enableBorderSide(Rectangle.LEFT);
	    cell.enableBorderSide(Rectangle.RIGHT);
		datatable.addCell(cell);

		cell = new Cell(new Phrase("\n", FontDataType));
		cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		cell.disableBorderSide(Rectangle.BOX);
	    cell.enableBorderSide(Rectangle.BOTTOM);
	    cell.enableBorderSide(Rectangle.LEFT);
	    cell.enableBorderSide(Rectangle.RIGHT);
		datatable.addCell(cell);

		return datatable;
	}

	private Table createBlankTable(int count) throws Exception {
		Table datatable = new Table(11);

		datatable.setPadding(IReport.CELLPADDING);
		datatable.setSpacing(0);
		datatable.setWidths(tableHeaderwidths);
		datatable.setWidth(100);
		datatable.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
		datatable.setDefaultVerticalAlignment(Element.ALIGN_CENTER);
		datatable.setBorder(Rectangle.NO_BORDER);
		datatable.setSpaceInsideCell(3);

		String strNumber = String.valueOf(count);

		Cell cell = null;

		cell = new Cell(new Phrase(strNumber, FontDataType));
		cell.setRowspan(2);
		cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
		cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		cell.setUseAscender(true);
		datatable.addCell(cell);

		cell = new Cell(new Phrase("\n\n", FontDataType));
		cell.setRowspan(2);
		cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		cell.setUseAscender(true);
		datatable.addCell(cell);

		cell = new Cell(new Phrase("", FontDataType));
		cell.setRowspan(2);
		cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
		cell.setUseAscender(true);
		datatable.addCell(cell);

		cell = new Cell(new Phrase("", FontDataType));
		cell.setRowspan(2);
		cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
		cell.setUseAscender(true);
		datatable.addCell(cell);

		cell = new Cell(new Phrase("", FontDataType));
		cell.setRowspan(2);
		cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		cell.setUseAscender(true);
		datatable.addCell(cell);

		cell = new Cell(new Phrase("", FontDataType));
		cell.setRowspan(2);
		cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		cell.setUseAscender(true);
		datatable.addCell(cell);

		cell = new Cell(new Phrase("", FontDataType));
		cell.setRowspan(2);
		cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		cell.setUseAscender(true);
		datatable.addCell(cell);

		cell = new Cell(new Phrase("", FontDataType));
		cell.setRowspan(2);
		cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		cell.setUseAscender(true);
		datatable.addCell(cell);

		cell = new Cell(new Phrase("", FontDataType));
		cell.setRowspan(2);
		cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		cell.setUseAscender(true);
		datatable.addCell(cell);

		cell = new Cell(new Phrase("", FontDataType));
		cell.setRowspan(2);
		cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		cell.setUseAscender(true);
		datatable.addCell(cell);

		cell = new Cell(new Phrase("", FontDataType));
		cell.setRowspan(2);
		cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		cell.setUseAscender(true);
		datatable.addCell(cell);

		return datatable;
	}

    private void createSummaryTable() throws Exception {
        Table tableSummary = new Table(11);

        tableSummary.setPadding(IReport.CELLPADDING);
        tableSummary.setSpacing(0);
        tableSummary.setWidths(tableHeaderwidths);
        tableSummary.setWidth(100);
        tableSummary.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
        tableSummary.setDefaultVerticalAlignment(Element.ALIGN_MIDDLE);
        tableSummary.setAlignment(Element.ALIGN_CENTER);
        tableSummary.setBorder(Rectangle.NO_BORDER);
        tableSummary.setSpaceInsideCell(3);

        Cell cell = null;

        cell = new Cell(new Phrase("REASON: " + getReasonDesc(), FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_TOP);
        cell.setColspan(5);
        cell.setRowspan(5);
        cell.setMaxLines(5);
        cell.disableBorderSide(Rectangle.BOX);
        cell.setUseAscender(true);
        tableSummary.addCell(cell);

        String strTotalAmountExl;
        String strGstAmount;
        String strTotalAmountIncl;
        String strTotalAmountAfterAdjust;
        String valueOfGstAmount;
        String valueOfTotalAmountIncl;
        if (taxRptFormat.equals("1")) {
            strTotalAmountExl = "TOTAL AMOUNT EXCLUSIVE " + adlangmstSQL.getTranslatedCaptionMsg(USER_LANGUAGE, "GST") +
                                " (" + SYSCurrCom + ")";
            strGstAmount = adlangmstSQL.getTranslatedCaptionMsg(USER_LANGUAGE, "GST") + " AMOUNT (" + SYSCurrCom + ")";
            valueOfGstAmount = currencyConverter.format(cntxhdrSQL.SALES_VAT_AMT_TOT());
            strTotalAmountIncl = "TOTAL AMOUNT INCLUSIVE " + adlangmstSQL.getTranslatedCaptionMsg(USER_LANGUAGE, "GST") +
                                " (" + SYSCurrCom + ")";
            valueOfTotalAmountIncl = currencyConverter.format(cntxhdrSQL.SELL_RTN_WVAT_TOT());
            strTotalAmountAfterAdjust = "TOTAL AMOUNT INCLUSIVE " + adlangmstSQL.getTranslatedCaptionMsg(USER_LANGUAGE, "GST") +
                                " (AFTER ADJUSTMENT) (" + SYSCurrCom + ")";
        } else {
            strTotalAmountExl = "TOTAL AMOUNT (" + SYSCurrCom + ")";
            strGstAmount = "\n";
            valueOfGstAmount = "\n";
            strTotalAmountIncl = "\n";
            valueOfTotalAmountIncl = "\n";
            strTotalAmountAfterAdjust = "TOTAL AMOUNT (AFTER ADJUSTMENT) (" + SYSCurrCom + ")";
        }

        cell = new Cell(new Phrase(strTotalAmountExl, FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setColspan(5);
        cell.enableBorderSide(Rectangle.BOX);
        cell.setUseAscender(true);
        tableSummary.addCell(cell);

        cell = new Cell(new Phrase(currencyConverter.format(cntxhdrSQL.SELL_RTN_WOVAT_TOT()), FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.enableBorderSide(Rectangle.BOX);
        cell.setUseAscender(true);
        tableSummary.addCell(cell);

        // -----------------------------------------------------------------------------------------------//

        cell = new Cell(new Phrase(strGstAmount, FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setColspan(5);
        cell.enableBorderSide(Rectangle.BOX);
        cell.setUseAscender(true);
        tableSummary.addCell(cell);

        cell = new Cell(new Phrase(valueOfGstAmount, FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.enableBorderSide(Rectangle.BOX);
        cell.setUseAscender(true);
        tableSummary.addCell(cell);

        // -----------------------------------------------------------------------------------------------//

        cell = new Cell(new Phrase(strTotalAmountIncl, FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setColspan(5);
        cell.enableBorderSide(Rectangle.BOX);
        cell.setUseAscender(true);
        tableSummary.addCell(cell);

        cell = new Cell(new Phrase(valueOfTotalAmountIncl, FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.enableBorderSide(Rectangle.BOX);
        cell.setUseAscender(true);
        tableSummary.addCell(cell);

        // -----------------------------------------------------------------------------------------------//

        cell = new Cell(new Phrase("ROUNDING ADJUSTMENT " + " (" + SYSCurrCom + ")", FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setColspan(5);
        cell.enableBorderSide(Rectangle.BOX);
        cell.setUseAscender(true);
        tableSummary.addCell(cell);

        cell = new Cell(new Phrase(currencyConverter.format(cntxhdrSQL.ROUNDING_AMT()), FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.enableBorderSide(Rectangle.BOX);
        cell.setUseAscender(true);
        tableSummary.addCell(cell);

        // -----------------------------------------------------------------------------------------------//

        cell = new Cell(new Phrase(strTotalAmountAfterAdjust, FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setColspan(5);
        cell.enableBorderSide(Rectangle.BOX);
        cell.setUseAscender(true);
        tableSummary.addCell(cell);

        double tot_AMT_After_ADJ = cntxhdrSQL.SELL_RTN_WVAT_TOT() + cntxhdrSQL.ROUNDING_AMT();
        cell = new Cell(new Phrase(currencyConverter.format(tot_AMT_After_ADJ), FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.enableBorderSide(Rectangle.BOX);
        cell.setUseAscender(true);
        tableSummary.addCell(cell);

        document.add(tableSummary);
    }

        private void createBlankSummaryTable() throws Exception {
        Table tableSummary = new Table(11);

        tableSummary.setPadding(IReport.CELLPADDING);
        tableSummary.setSpacing(0);
        tableSummary.setWidths(tableHeaderwidths);
        tableSummary.setWidth(100);
        tableSummary.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
        tableSummary.setDefaultVerticalAlignment(Element.ALIGN_MIDDLE);
        tableSummary.setBorder(Rectangle.NO_BORDER);
        tableSummary.setSpaceInsideCell(3);

        Cell cell = null;

        cell = new Cell(new Phrase("REASON: " + getReasonDesc(), FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_TOP);
        cell.setColspan(5);
        cell.setRowspan(5);
        cell.setMaxLines(5);
        cell.disableBorderSide(Rectangle.BOX);
        cell.setUseAscender(true);
        tableSummary.addCell(cell);

        String strTotalAmountExl;
        String strGstAmount;
        String strTotalAmountIncl;
        String strTotalAmountAfterAdjust;
        if (taxRptFormat.equals("1")) {
            strTotalAmountExl = "TOTAL AMOUNT EXCLUSIVE " + adlangmstSQL.getTranslatedCaptionMsg(USER_LANGUAGE, "GST") +
                                " (" + SYSCurrCom + ")";
            strGstAmount = adlangmstSQL.getTranslatedCaptionMsg(USER_LANGUAGE, "GST") + " AMOUNT (" + SYSCurrCom + ")";
            strTotalAmountIncl = "TOTAL AMOUNT INCLUSIVE " + adlangmstSQL.getTranslatedCaptionMsg(USER_LANGUAGE, "GST") +
                                " (" + SYSCurrCom + ")";
            strTotalAmountAfterAdjust = "TOTAL AMOUNT INCLUSIVE " + adlangmstSQL.getTranslatedCaptionMsg(USER_LANGUAGE, "GST") +
                                " (AFTER ADJUSTMENT) (" + SYSCurrCom + ")";
        } else {
            strTotalAmountExl = "TOTAL AMOUNT (" + SYSCurrCom + ")";
            strGstAmount = "\n";
            strTotalAmountIncl = "\n";
            strTotalAmountAfterAdjust = "TOTAL AMOUNT (AFTER ADJUSTMENT) (" + SYSCurrCom + ")";
        }

        cell = new Cell(new Phrase(strTotalAmountExl, FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setColspan(5);
        cell.enableBorderSide(Rectangle.BOX);
        cell.setUseAscender(true);
        tableSummary.addCell(cell);

        cell = new Cell(new Phrase(""));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.enableBorderSide(Rectangle.BOX);
        cell.setUseAscender(true);
        tableSummary.addCell(cell);

        // -----------------------------------------------------------------------------------------------//

        cell = new Cell(new Phrase(strGstAmount, FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setColspan(5);
        cell.enableBorderSide(Rectangle.BOX);
        cell.setUseAscender(true);
        tableSummary.addCell(cell);

        cell = new Cell(new Phrase(""));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.enableBorderSide(Rectangle.BOX);
        cell.setUseAscender(true);
        tableSummary.addCell(cell);

        // -----------------------------------------------------------------------------------------------//

        cell = new Cell(new Phrase(strTotalAmountIncl, FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setColspan(5);
        cell.enableBorderSide(Rectangle.BOX);
        cell.setUseAscender(true);
        tableSummary.addCell(cell);

        cell = new Cell(new Phrase(""));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.enableBorderSide(Rectangle.BOX);
        cell.setUseAscender(true);
        tableSummary.addCell(cell);

        // -----------------------------------------------------------------------------------------------//

        cell = new Cell(new Phrase("ROUNDING ADJUSTMENT " + " (" + SYSCurrCom + ")", FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setColspan(5);
        cell.enableBorderSide(Rectangle.BOX);
        cell.setUseAscender(true);
        tableSummary.addCell(cell);

        cell = new Cell(new Phrase("", FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.enableBorderSide(Rectangle.BOX);
        cell.setUseAscender(true);
        tableSummary.addCell(cell);

        // -----------------------------------------------------------------------------------------------//

        cell = new Cell(new Phrase(strTotalAmountAfterAdjust, FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setColspan(5);
        cell.enableBorderSide(Rectangle.BOX);
        cell.setUseAscender(true);
        tableSummary.addCell(cell);

        cell = new Cell(new Phrase("", FontTotalType));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.enableBorderSide(Rectangle.BOX);
        cell.setUseAscender(true);
        tableSummary.addCell(cell);

        document.add(tableSummary);
    }

	private String getReasonDesc() throws Exception {
		String strReasonDesc = "";
		String query = "SELECT MSG_DESC "
				+ "FROM MSGMST "
				+ "WHERE MSG_CD = ? "
				+ "AND MSG_GROUP = 'CR' ";

		Parameters parameters = Parameters.builder(1)
					.set(cntxhdrSQL.CN_RSN_CD())
					.build();

		ResultSet rs = manager.select(query, parameters);

		if (rs != null && rs.next()) {
			strReasonDesc = rs.getString("MSG_DESC");
		}
		return getDescription(strReasonDesc);
	}

	private void printTableFooter(String strCN_TYPE) throws BadElementException,
			DocumentException, SQLException {
		Table totalTable = new Table(1);

		int headerwidths[] = { 100 };

		totalTable.setPadding(0.5f);
		totalTable.setSpacing(0);
		totalTable.setWidths(headerwidths);
		totalTable.setWidth(100);
		totalTable.setDefaultHorizontalAlignment(Element.ALIGN_CENTER);
		totalTable.setBorder(Rectangle.NO_BORDER);

		String strPages = "Page " + String.valueOf(intPageNum) + "/" + String.valueOf(intPageTotal);

		int intLineCount = 7;
		if (!strCN_TYPE.equals("Q")) {
			intLineCount = 6;
		}

		Cell cell = null;
		
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

	private void updateCNTXHDR(CntxhdrSQL cntxhdrSQL) throws Exception {
		cntxhdrSQL.getByKey();

		cntxhdrSQL.setCN_PRINTED_DATE(sysctlSQL.getLAST_DAY_UPDATEDPlusOne());
		cntxhdrSQL.setCN_PRINTED_BY(strUSER_ID);
		cntxhdrSQL.setLAST_OPR(strUSER_ID);
		cntxhdrSQL.setLAST_OPR_FUNCT("PRINT_RTN_CN");
		cntxhdrSQL.update();
	}
}