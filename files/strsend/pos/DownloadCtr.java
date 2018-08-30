/**                                                      
 * Copyright (C) 2006 QR Retail Autotamation Sdn. Bhd.  
 * All right reserved.                                  
 *                                                      
 */
/**                                                      
 * Synopsis:  
 *                                                      
 * Written: Profit V7.0 Project Team: Aw Kien Woon 2005/06
 *                                                      
 * Revised: Name/ Date.                                 
 * Khim 2012-10-18 Update EODQUEUESTATUS.QSTATUS if pass in from EOD (Ref: AU-SD003858559 (AU-IM001013432))
 * JoeMy 2013-09-12 ASEAN PCR
 * JoeMy 2014-03-11 Member Discount Item - ASEAN PCR
 * guoliang 2014-05-21 AU-IM002701122 set PosFileInfo.COY_SUB for use by PosItemmstDownload and PosItemmstDownloadASEAN
 * JoeMy 2014-08-13 WINCOR PCR
 * JoanChia 2014-08-20 WINCOR PCR (promotion) 
 * JoeMy 2015-01-12 POS DOS and Q5 PCR 
 */

package qrcom.PROFIT.files.strsend.pos;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;

import qrcom.PROFIT.batch.EOD.StatusWriter;
import qrcom.PROFIT.files.info.AduserSQL;
import qrcom.PROFIT.files.info.LockermstSQL;
import qrcom.PROFIT.files.info.ProfitvvSQL;
import qrcom.PROFIT.files.info.StrmstInfo;
import qrcom.PROFIT.files.info.SysctlSQL;
import qrcom.PROFIT.files.strsend.poswincor.WincorItemmstDownload;
import qrcom.PROFIT.files.strsend.poswincor.WincorItemmstExtDownload;
import qrcom.PROFIT.files.strsend.poswincor.WincorLineDivDeptDownload;
import qrcom.PROFIT.files.strsend.poswincor.WincorTplDiscLin;
import qrcom.PROFIT.files.strsend.poswincor.WincorTplGqmst;
import qrcom.PROFIT.files.strsend.poswincor.WincorTplLiqDet;
import qrcom.PROFIT.files.strsend.poswincor.WincorTplLiqHdr;
import qrcom.PROFIT.files.strsend.poswincor.WincorTplPromo;
import qrcom.PROFIT.files.strsend.poswincor.WincorTplRuleDetCombination;
import qrcom.PROFIT.files.strsend.poswincor.WincorTplRuleDetLine;
import qrcom.PROFIT.files.strsend.poswincor.WincorTplRuleDetQuantity;
import qrcom.PROFIT.files.strsend.poswincor.WincorTplRuleHdr;
import qrcom.PROFIT.files.strsend.poswincor.WincorXbarcodeDownload;
import qrcom.PROFIT.shared.Utility.ProfitvvManager;
import qrcom.PROFIT.system.SysParam;
import qrcom.util.HParam;
import qrcom.util.qrMisc;
import qrcom.util.ejb.connection.DataSource;

public class DownloadCtr {
	private Connection conn;
	private Connection conn_autoCommit;
	private ArrayList STORELIST;
	private String sStore, sCoySub, sCoy, sPosType;
	private String Dwn_Type = ""; // Added by kofam 2017-03-30 AVN - Interface with E-Commerce
	private boolean bPosDwn = true; // Added by kofam 2017-03-30 AVN - Interface with E-Commerce
	private boolean bPromoType = false; // Added by kofam 2017-03-30 AVN - Interface with E-Commerce

	private StrmstInfo ssStoreInfo;
	private StatusWriter statusWriter;

	private PosDivmstDownload divmstPos;
	private PosDeptmstDownload deptmstPos;
	private PosClassmstDownload classmstPos;
	private PosSbclsmstDownload sbclsmstPos;
	private PosSbclsmstDownloadASEAN sbclsmstPosASEAN;
	private PosDownloadProp posDownloadProp;
	private DeleteAmended deleteAmended;
	private ProfitvvManager profitvvManager; // Added by kofam 2017-03-30 AVN - Interface with E-Commerce

	private PosItemmstDownload itemPos = null;
	private PosItemmstDownloadASEAN itemPosASEAN = null;
	private PosItemmstDownloadASEANAIN itemPosASEANAIN = null; // SYCHUA 2015-03-16 - AIN PCR
	private PosXbarcodeDownload xbarcodePos = null;
	private PosPrcChgDownload prcChg = null;
	private PosMixmPmtDownload mixmPmt = null;
	private PosMixmComDownload mixmCom = null;
	private PosPrcChgDownloadASEAN prcChgASEAN = null;
	private PosMixmPmtDownloadASEAN mixmPmtASEAN = null;
	private PosMixmComDownloadASEAN mixmComASEAN = null;
	private PosGrpPrcChgDownload grpPrcChg = null;

	private WincorLineDivDeptDownload wincorLineDivDept = null;
	private WincorXbarcodeDownload wincorXbarcode = null;
	private WincorItemmstDownload wincorItem = null;
	private WincorItemmstExtDownload wincoritemmstext = null;

	private WincorTplGqmst wincortplgqmst = null;
	private WincorTplPromo wincortplpromo = null;
	private WincorTplRuleHdr wincortplrulehdr = null;
	private WincorTplRuleDetLine wincortplruledetline = null;
	private WincorTplRuleDetQuantity wincortplruledetquantity = null;
	private WincorTplRuleDetCombination wincortplruledetcombination = null;
	private WincorTplLiqHdr wincortplliqhdr = null;
	private WincorTplLiqDet wincortplliqdet = null;
	private WincorTplDiscLin wincortpdisclin = null;

	public DownloadCtr() {
		openConnection();
	}

	public void setStatusWriter(StatusWriter _statusWriter) {
		this.statusWriter = _statusWriter;
	}

	/* Added by Khim 20091223 - Allow pass in COY parameter */
	// JoeMy 2014-08-13 WINCOR PCR
	public void initialize(String coysub, String store, String lang,
			String coy, String posType) {

		this.sStore = store;
		this.sCoySub = coysub;
		this.sCoy = coy;
		this.sPosType = posType; // JoeMy 2014-08-13 WINCOR PCR

		try {
			// PosFileInfo.LANGCODE = lang;
			PosFileInfo.LANGCODE = SysParam.getInstance().getResSystem("Default_language_code");
		} catch (Exception e) {
			PosFileInfo.LANGCODE = "1";
		}

		PosFileInfo.COY = sCoy;
		PosFileInfo.COY_SUB = coysub; // PosItemmstDownload needs this / added guoliang

		try {
			ProfitvvSQL profitvvSQL = new ProfitvvSQL(conn);
			PosFileInfo.SYSBarConsNPLUPrefix = profitvvSQL.getProfitvvValueByPrimaryKey(sCoy, "SYSBarConsNPLUPrefix");
			PosFileInfo.SYSBarNonPLUPrefix = profitvvSQL.getProfitvvValueByPrimaryKey(sCoy, "SYSBarNonPLUPrefix");
			// JoeMy 2013-09-12 ASEAN PCR
			PosFileInfo.SYSPosMethod = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSPosMethod");
			PosFileInfo.SYSMultiInHouseBar = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSMultiInHouseBar");
			PosFileInfo.SYSBarNormNPLUPrefix = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSBarNormNPLUPrefix");
			PosFileInfo.SYSPosCtrlItem = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSPosCtrlItem");
			// JoeMy 2014-08-13 WINCOR PCR
			PosFileInfo.SYSSeitoPOS = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSSeitoPOS");
			PosFileInfo.SYSWincorPOS = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSWincorPOS");
			PosFileInfo.SYSEnbWincorPOSBck = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSEnbWincorPOSBck");
			PosFileInfo.SYSEnbWincorPOSBingo = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSEnbWincorPOSBingo");
			PosFileInfo.SYSWrtWincorPOSBingo = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSWrtWincorPOSBingo");

			// JoeMy 2014-08-27 WINCOR PCR, same timestamp in one batch
			PosFileInfo.FILENAME = getFilename();
			// Joan 2014-08-20 WINCOR PCR -Promotion
			PosFileInfo.SYSClassLength = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSClassLength");
			PosFileInfo.SYSDeptLength = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSDeptLength");
			PosFileInfo.SYSDivLength = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSDivLength");
			PosFileInfo.SYSPmgrpLength = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSPmgrpLength");
			PosFileInfo.SYSBarcodeLength = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSBarcodeLength");
			PosFileInfo.SYSSbClsLength = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSSbClsLength");
			PosFileInfo.SYSSkuMax = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSSkuMax");
			PosFileInfo.SYSSubCatLength = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSSubCatLength");
			PosFileInfo.SYSTPLDwnMaxAmt = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSTPLDwnMaxAmt");
			PosFileInfo.SYSTPLDwnMaxQty = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSTPLDwnMaxQty");

			// JoeMy 2015-01-12 POS DOS and Q5 PCR
			PosFileInfo.SYSSeitoPOSQ5 = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSSeitoPOSQ5");

			// JoeMy 2015-01-30 GST PCR
			PosFileInfo.SYSSellVATBefGST = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSSellVATBefGST");
			PosFileInfo.SYSDwnDefSellVat = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSDwnDefSellVat");

			// SYCHUA 2015-03-16 AIN PCR
			PosFileInfo.SYSPosMethodAsean = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSPosMethodAsean");
			PosFileInfo.SYSPosBarFillMethod = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSPosBarFillMethod");

			PosFileInfo.SYSEcomDownload = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSEcomDownload"); // Added by kofam 2017-03-30 AVN - Interface with E-Commerce
			PosFileInfo.SYSEcomDwnBackup = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSEcomDwnBackup"); // Added by kofam 2017-03-30 AVN - Interface with E-Commerce
			PosFileInfo.SYSEcomDwnStore = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSEcomDwnStore"); // Added by kofam 2017-03-30 AVN - Interface with E-Commerce

			PosFileInfo.SYSAddAutoDiscFlg = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSAddAutoDiscFlg"); // Added by alanheng 2017-07-24 - SRAM067156 Additional Auto Discount System Enhancement
                    
			PosFileInfo.SYSSalesTaxCode = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSSalesTaxCode");
		} catch (Exception e) {
			PosFileInfo.SYSBarConsNPLUPrefix = "23";
			PosFileInfo.SYSBarNonPLUPrefix = "22";
			// JoeMy 2013-09-12 ASEAN PCR
			PosFileInfo.SYSPosMethod = PosFileInfo.PosAMY;
			PosFileInfo.SYSMultiInHouseBar = "N";
			PosFileInfo.SYSBarNormNPLUPrefix = "23";
			PosFileInfo.SYSPosCtrlItem = "N";
			// JoeMy 2014-08-13 WINCOR PCR
			PosFileInfo.SYSSeitoPOS = "I";
			PosFileInfo.SYSWincorPOS = "W";
			PosFileInfo.SYSEnbWincorPOSBck = "Y";
			PosFileInfo.SYSEnbWincorPOSBingo = "Y";
			PosFileInfo.SYSWrtWincorPOSBingo = "Y";
			//
			PosFileInfo.SYSClassLength = "4";
			PosFileInfo.SYSDeptLength = "3";
			PosFileInfo.SYSDivLength = "2";
			PosFileInfo.SYSPmgrpLength = "1";
			PosFileInfo.SYSBarcodeLength = "13";
			PosFileInfo.SYSSbClsLength = "6";
			PosFileInfo.SYSSkuMax = "8";
			PosFileInfo.SYSSubCatLength = "2";
			PosFileInfo.SYSTPLDwnMaxAmt = "24";
			PosFileInfo.SYSTPLDwnMaxQty = "13";
			// JoeMy 2015-01-12 POS DOS and Q5 PCR
			PosFileInfo.SYSSeitoPOSQ5 = "Q";
			// JoeMy 2015-01-30 GST PCR
			PosFileInfo.SYSSellVATBefGST = "0";
			PosFileInfo.SYSDwnDefSellVat = "N";
			// SYCHUA 2015-03-16 AIN PCR
			PosFileInfo.SYSPosMethodAsean = "1";
			PosFileInfo.SYSPosBarFillMethod = "1";
			PosFileInfo.SYSEcomDwnStore = ""; // Added by kofam 2017-03-30 AVN - Interface with E-Commerce
			PosFileInfo.SYSEcomDownload = ""; // Added by kofam 2017-03-30 AVN - Interface with E-Commerce
			PosFileInfo.SYSEcomDwnBackup = ""; // Added by kofam 2017-03-30 AVN - Interface with E-Commerce
			PosFileInfo.SYSAddAutoDiscFlg = ""; // Added by alanheng 2017-07-24 - SRAM067156 Additional Auto Discount System Enhancement
                        
			PosFileInfo.SYSSalesTaxCode = "0";
		}

	}

	// note: lang parameter not used
	public void initialize(String coysub, String store, String lang) {
		this.sStore = store;
		this.sCoySub = coysub;

		try {
			// PosFileInfo.LANGCODE = lang;
			PosFileInfo.LANGCODE = SysParam.getInstance().getResSystem("Default_language_code");
		} catch (Exception e) {
			PosFileInfo.LANGCODE = "1";
		}
		PosFileInfo.COY = getCoy();
		PosFileInfo.COY_SUB = coysub; // PosItemmstDownload needs this / added guoliang

		try {
			ProfitvvSQL profitvvSQL = new ProfitvvSQL(conn);
			PosFileInfo.SYSBarConsNPLUPrefix = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSBarConsNPLUPrefix");
			PosFileInfo.SYSBarNonPLUPrefix = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSBarNonPLUPrefix");
			// JoeMy 2013-09-12 ASEAN PCR
			PosFileInfo.SYSPosMethod = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSPosMethod");
			PosFileInfo.SYSMultiInHouseBar = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSMultiInHouseBar");
			PosFileInfo.SYSBarNormNPLUPrefix = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSBarNormNPLUPrefix");
			PosFileInfo.SYSPosCtrlItem = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSPosCtrlItem");
			// JoeMy 2014-08-13 WINCOR PCR
			PosFileInfo.SYSSeitoPOS = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSSeitoPOS");
			PosFileInfo.SYSWincorPOS = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSWincorPOS");
			PosFileInfo.SYSEnbWincorPOSBck = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSEnbWincorPOSBck");
			PosFileInfo.SYSEnbWincorPOSBingo = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSEnbWincorPOSBingo");
			PosFileInfo.SYSWrtWincorPOSBingo = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSWrtWincorPOSBingo");
			// JoeMy 2015-01-12 POS DOS and Q5 PCR
			PosFileInfo.SYSSeitoPOSQ5 = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSSeitoPOSQ5");
			// JoeMy 2015-01-30 GST PCR
			PosFileInfo.SYSSellVATBefGST = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSSellVATBefGST");
			PosFileInfo.SYSDwnDefSellVat = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSDwnDefSellVat");

			PosFileInfo.SYSPosMethodAsean = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSPosMethodAsean");
			PosFileInfo.SYSPosBarFillMethod = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSPosBarFillMethod");

			PosFileInfo.SYSEcomDownload = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSEcomDownload"); // Added by kofam 2017-03-30 AVN - Interface with E-Commerce
			PosFileInfo.SYSEcomDwnBackup = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSEcomDwnBackup"); // Added by kofam 2017-03-30 AVN - Interface with E-Commerce
			PosFileInfo.SYSEcomDwnStore = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSEcomDwnStore"); // Added by kofam 2017-03-30 AVN - Interface with E-Commerce

			PosFileInfo.SYSAddAutoDiscFlg = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSAddAutoDiscFlg"); // Added by alanheng 2017-07-24 - SRAM067156 Additional Auto Discount System Enhancement
                    
			PosFileInfo.SYSSalesTaxCode = profitvvSQL.getProfitvvValueByPrimaryKey(PosFileInfo.COY, "SYSSalesTaxCode");
		} catch (Exception e) {
			PosFileInfo.SYSBarConsNPLUPrefix = "23";
			PosFileInfo.SYSBarNonPLUPrefix = "22";
			// JoeMy 2013-09-12 ASEAN PCR
			PosFileInfo.SYSPosMethod = PosFileInfo.PosAMY;
			PosFileInfo.SYSMultiInHouseBar = "N";
			PosFileInfo.SYSBarNormNPLUPrefix = "23";
			PosFileInfo.SYSPosCtrlItem = "N";
			// JoeMy 2014-08-13 WINCOR PCR
			PosFileInfo.SYSSeitoPOS = "I";
			PosFileInfo.SYSWincorPOS = "W";
			PosFileInfo.SYSEnbWincorPOSBck = "Y";
			PosFileInfo.SYSEnbWincorPOSBingo = "Y";
			PosFileInfo.SYSWrtWincorPOSBingo = "Y";
			// JoeMy 2015-01-12 POS DOS and Q5 PCR
			PosFileInfo.SYSSeitoPOSQ5 = "Q";
			// JoeMy 2015-01-30 GST PCR
			PosFileInfo.SYSSellVATBefGST = "0";
			PosFileInfo.SYSDwnDefSellVat = "N";

			// SYCHUA 2015-03-16 AIN PCR
			PosFileInfo.SYSPosMethodAsean = "1";
			PosFileInfo.SYSPosBarFillMethod = "1";

			PosFileInfo.SYSEcomDwnStore = ""; // Added by kofam 2017-03-30 AVN - Interface with E-Commerce
			PosFileInfo.SYSEcomDownload = ""; // Added by kofam 2017-03-30 AVN - Interface with E-Commerce
			PosFileInfo.SYSEcomDwnBackup = ""; // Added by kofam 2017-03-30 AVN - Interface with E-Commerce
			PosFileInfo.SYSEcomDwnStore = ""; // Added by kofam 2017-03-30 AVN - Interface with E-Commerce
			PosFileInfo.SYSAddAutoDiscFlg = ""; // Added by alanheng 2017-07-24 - SRAM067156 Additional Auto Discount System Enhancement
                        
			PosFileInfo.SYSSalesTaxCode = "0";
		}
	}

	private String getCoy() {
		String coy = sCoySub;
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement("SELECT COY FROM COYSUBMST WHERE COY_SUB=?");
			pstmt.setString(1, sCoySub);
			rs = pstmt.executeQuery();
			if (rs != null && rs.next()) {
				coy = rs.getString("COY");
			}
		} catch (Exception sqle) {
			sqle.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
			rs = null;

			try {
				if (pstmt != null)
					pstmt.close();
			} catch (Exception e) {
			}
			pstmt = null;
		}

		return coy;
	}

	public ArrayList getCoyList() {
		ResultSet r = null;
		PreparedStatement p = null;
		ArrayList coysubList = new ArrayList();

		try {
			p = conn.prepareStatement("SELECT COY_SUB FROM COYSUBMST");
			r = p.executeQuery();
			while (r != null && r.next()) {
				coysubList.add(r.getString("COY_SUB"));
			}
		} catch (Exception e) {
			coysubList.add("001");
			coysubList.add("002");
			coysubList.add("003");
			e.printStackTrace();
		} finally {
			try {
				if (r != null)
					r.close();
			} catch (Exception e) {
			}
			r = null;

			try {
				if (p != null)
					p.close();
			} catch (Exception e) {
			}
			p = null;
		}
		return coysubList;
	}

	/* Edited by Khim 20091223 */
	public void start() throws Exception {
		posDownloadProp = new PosDownloadProp(conn);
		posDownloadProp.setDownloadPath();

		try {
			/*
			 * Remarked by Khim 20100104 - 'All' replaced to '' if
			 * (sStore.equals("All")) { //PosFileInfo.ALL = true; STORELIST = posDownloadProp.setStoreList(false, "", sCoySub); }
			 */

			if (sStore != null && sStore.trim().length() == 0) {
				// JoeMy 2014-08-13 WINCOR PCR
				// STORELIST = posDownloadProp.setStoreList(false, "", sCoySub, sCoy);
				STORELIST = posDownloadProp.setStoreList(false, "", sCoySub, sCoy, sPosType, Dwn_Type, bPosDwn);
			} else {
				// PosFileInfo.ALL = false;

				// JoeMy 2014-08-13 WINCOR PCR
				// STORELIST = posDownloadProp.setStoreList(true, sStore, sCoySub, sCoy);
				STORELIST = posDownloadProp.setStoreList(true, sStore, sCoySub, sCoy, sPosType, Dwn_Type, bPosDwn);
			}
		} catch (Exception e) {
			throw (e);
		}
	}

	/* Goxz 20170822 No longer used, so removed this method
	 Added by Khim 20091224 - Control all download data table type by one Loop 
	public void generateDwnFile(char cMasterTblFlg, char cItemFlg,
			char cBarcodeFlg, char cPromotionFlg, char cGrpPrcChgFlg)
			throws Exception {
		if (STORELIST == null)
			STORELIST = new ArrayList();

		// MT Table
		divmstPos = new PosDivmstDownload(conn, STORELIST);
		deptmstPos = new PosDeptmstDownload(conn, STORELIST);
		classmstPos = new PosClassmstDownload(conn, STORELIST);
		sbclsmstPos = new PosSbclsmstDownload(conn, STORELIST);

		// Item
		itemPos = new PosItemmstDownload(conn, STORELIST);

		// JoeMy 2013-09-17 ASEAN PCR
		itemPosASEAN = new PosItemmstDownloadASEAN(conn, STORELIST);

		// SYCHUA 2015-03-16 AIN PCR
		itemPosASEANAIN = new PosItemmstDownloadASEANAIN(conn, STORELIST);

		// Barcode
		xbarcodePos = new PosXbarcodeDownload(conn, STORELIST);

		// Promotion
		prcChg = new PosPrcChgDownload(conn, STORELIST);
		mixmPmt = new PosMixmPmtDownload(conn, STORELIST);
		mixmCom = new PosMixmComDownload(conn, STORELIST);

		// JoeMy 2013-09-12 ASEAN PCR
		prcChgASEAN = new PosPrcChgDownloadASEAN(conn, STORELIST);
		mixmPmtASEAN = new PosMixmPmtDownloadASEAN(conn, STORELIST);
		mixmComASEAN = new PosMixmComDownloadASEAN(conn, STORELIST);

		// JoeMy 2014-03-11 Member Discount Item
		sbclsmstPosASEAN = new PosSbclsmstDownloadASEAN(conn, STORELIST);

		// JoeMy 2014-08-13 WINCOR PCR
		wincorLineDivDept = new WincorLineDivDeptDownload(conn, STORELIST);
		wincorXbarcode = new WincorXbarcodeDownload(conn, STORELIST);
		wincorItem = new WincorItemmstDownload(conn, STORELIST);
		wincoritemmstext = new WincorItemmstExtDownload(conn, STORELIST);

		// Group price change
		grpPrcChg = new PosGrpPrcChgDownload(conn, STORELIST);

		try {
			for (int i = 0; i < STORELIST.size(); i++) {
				ssStoreInfo = (StrmstInfo) STORELIST.get(i);
				writeLog("Download for Store: " + ssStoreInfo.STORE());

				if (cMasterTblFlg != 'n') {
					writeLog("*** Generate ascii file for Master Tables ***");
					callCreateMaster(cMasterTblFlg, i);
					writeLog("*** Finished generate ascii file for Master Tables ***");
				}

				if (cItemFlg != 'n') {
					writeLog("*** Generate ascii file for Item ***");
					callCreateItem(cItemFlg, i);
					writeLog("*** Finished generate ascii file for Item ***");
				}

				if (cBarcodeFlg != 'n') {
					writeLog("*** Generate ascii file for Barcode ***");
					callCreateBarcode(cBarcodeFlg, i);
					writeLog("*** Finished generate ascii file for Barcode ***");
				}

				if (cPromotionFlg != 'n') {
					writeLog("*** Generate ascii file for Promotion ***");
					callCreatePromotion(cPromotionFlg, i);
					writeLog("*** Finished generate ascii file for Promotion ***");
				}

				if (cGrpPrcChgFlg != 'n') {
					writeLog("*** Generate ascii file for Group Price Change ***");
					callCreateGrpPrcChg(cGrpPrcChgFlg, i);
					writeLog("*** Finished generate ascii file for Group Price Change ***");
				}

				writeLog("\r\n");

				if (sStore != null && sStore.trim().length() == 0) {
					writeLog("*** Pos Download for Company: " + sCoy
							+ " Store: All Stores completed ***");
				} else {
					writeLog("*** Pos Download for Company: " + sCoy
							+ " Store: " + sStore + " completed ***");
				}
			}
		} catch (Exception e) {
			throw (e);
		}
	}*/

	/* Added by khim 20091224 - without loop */
	private void callCreateMaster(char flag, int iLoop) throws Exception {
		if (flag == PosFileInfo.WHOLE) {
			downlodWholeMaster(iLoop);
		} else {
			downlodAmendedMaster(iLoop);
		}
	}

	public void callCreateMaster(char flag) throws Exception {
		if (STORELIST == null)
			STORELIST = new ArrayList();

		// Goxz 20170817 POS Mater file download process performance slow
		ArrayList<StrmstInfo> POSSTORELIST = new ArrayList<StrmstInfo>();
		ArrayList<StrmstInfo> WincorSTORELIST = new ArrayList<StrmstInfo>();
		for (Object info : STORELIST) {
			StrmstInfo strmstInfo = (StrmstInfo) info;
			if (isNotBlank(strmstInfo.STORE_POS_TYPE())
					&& strmstInfo.STORE_POS_TYPE().equals(PosFileInfo.SYSWincorPOS)) {
				WincorSTORELIST.add(strmstInfo);
			} else {
				POSSTORELIST.add(strmstInfo);
			}
		}

		divmstPos = new PosDivmstDownload(conn, POSSTORELIST);
		deptmstPos = new PosDeptmstDownload(conn, POSSTORELIST);
		classmstPos = new PosClassmstDownload(conn, POSSTORELIST);
		sbclsmstPos = new PosSbclsmstDownload(conn, POSSTORELIST);

		// JoeMy 2014-03-11 Member Discount Item
		sbclsmstPosASEAN = new PosSbclsmstDownloadASEAN(conn, POSSTORELIST);

		// JoeMy 2014-08-13 WINCOR PCR
		wincorLineDivDept = new WincorLineDivDeptDownload(conn, WincorSTORELIST);
		wincoritemmstext = new WincorItemmstExtDownload(conn, WincorSTORELIST);

		if (!POSSTORELIST.isEmpty()) {
			ssStoreInfo = (StrmstInfo) POSSTORELIST.get(0);
			if (flag == PosFileInfo.WHOLE) {
				downlodWholeMaster(0);
			} else {
				downlodAmendedMaster(0);
			}
		}
		if (!WincorSTORELIST.isEmpty()) {
			for (int i = 0; i < WincorSTORELIST.size(); i++) {
				ssStoreInfo = (StrmstInfo) WincorSTORELIST.get(i);
				if (flag == PosFileInfo.WHOLE) {
					downlodWholeMaster(i);
				} else {
					downlodAmendedMaster(i);
				}
			}
		}
			
		/*
		for (int i = 0; i < STORELIST.size(); i++) {
			ssStoreInfo = (StrmstInfo) STORELIST.get(i);
			if (flag == PosFileInfo.WHOLE) {
				downlodWholeMaster(i);
			} else {
				downlodAmendedMaster(i);
			}
		}
		*/

	}

	public void deleteAmendedMaster() throws Exception {
		if (STORELIST == null)
			STORELIST = new ArrayList();
		deleteAmended = new DeleteAmended(conn);
		divmstPos = new PosDivmstDownload(conn, STORELIST);
		deptmstPos = new PosDeptmstDownload(conn, STORELIST);
		classmstPos = new PosClassmstDownload(conn, STORELIST);
		sbclsmstPos = new PosSbclsmstDownload(conn, STORELIST);

		// JoeMy 2014-03-11 Member Discount Item
		sbclsmstPosASEAN = new PosSbclsmstDownloadASEAN(conn, STORELIST);

		// Goxz 20170817 POS Mater file download process performance slow - Start
		if (!STORELIST.isEmpty()) {
			divmstPos.deleteAmendedRecords(0, deleteAmended);
			deptmstPos.deleteAmendedRecords(0, deleteAmended);
			classmstPos.deleteAmendedRecords(0, deleteAmended);
			if (PosFileInfo.SYSPosMethod.equals(PosFileInfo.PosASEAN)) {
				sbclsmstPosASEAN.deleteAmendedRecords(0, deleteAmended);
			} else {
				sbclsmstPos.deleteAmendedRecords(0, deleteAmended);
			}
		}

		/*
		for (int i = 0; i < STORELIST.size(); i++) {
			ssStoreInfo = (StrmstInfo) STORELIST.get(i);

			divmstPos.deleteAmendedRecords(i, deleteAmended);
			// writeLog("Delete Store " + ssStoreInfo.STORE() + " Divmst");

			deptmstPos.deleteAmendedRecords(i, deleteAmended);
			// writeLog("Delete Store " + ssStoreInfo.STORE() + " Deptmst");

			classmstPos.deleteAmendedRecords(i, deleteAmended);
			// writeLog("Delete Store " + ssStoreInfo.STORE() + " Classmst");

			// JoeMy 2014-03-11 Member Discount Item
			if (PosFileInfo.SYSPosMethod.equals(PosFileInfo.PosASEAN)) {
				sbclsmstPosASEAN.deleteAmendedRecords(i, deleteAmended);
			} else {
				sbclsmstPos.deleteAmendedRecords(i, deleteAmended);
			}
			// writeLog("Delete Store " + ssStoreInfo.STORE() + " Sbclsmst");
		}
		*/
		// Goxz 20170817 POS Mater file download process performance slow - End
	}

	private void downlodWholeMaster(int index) throws Exception {
		// JoeMy 2014-08-13 WINCOR PCR
		if (ssStoreInfo.STORE_POS_TYPE() != null
				&& ssStoreInfo.STORE_POS_TYPE().equals(PosFileInfo.SYSWincorPOS)) {
			wincorLineDivDept.downloadWholeTable(index);
			wincoritemmstext.downloadWholeTable(index);
		} else {
			divmstPos.downloadWholeTable(index);
			deptmstPos.downloadWholeTable(index);
			classmstPos.downloadWholeTable(index);

			// JoeMy 2014-03-11 Member Discount Item
			if (PosFileInfo.SYSPosMethod.equals(PosFileInfo.PosASEAN)) {
				sbclsmstPosASEAN.downloadWholeTable(index);
			} else {
				sbclsmstPos.downloadWholeTable(index);
			}
		}
	}

	private void writeLog(String strMsg) {
		if (statusWriter != null) {
			statusWriter.write(strMsg);
		}
	}

	private void downlodAmendedMaster(int index) throws Exception {
		/* Remarked by Khim 20091224 */
		// ssStoreInfo = (StrmstInfo) STORELIST.get(index);

		// JoeMy 2014-08-13 WINCOR PCR
		if (ssStoreInfo.STORE_POS_TYPE() != null
				&& ssStoreInfo.STORE_POS_TYPE().equals(PosFileInfo.SYSWincorPOS)) {
			wincorLineDivDept.downloadAmendedRecords(index);
			wincoritemmstext.downloadAmendedRecords(index);
		} else {
			divmstPos.downloadAmendedRecords(index);
			// writeLog("Store " + ssStoreInfo.STORE() + " Divmst");

			deptmstPos.downloadAmendedRecords(index);
			// writeLog("Store " + ssStoreInfo.STORE() + " Deptmst");

			classmstPos.downloadAmendedRecords(index);
			// writeLog("Store " + ssStoreInfo.STORE() + " Classmst");

			// JoeMy 2014-03-11 Member Discount Item
			if (PosFileInfo.SYSPosMethod.equals(PosFileInfo.PosASEAN)) {
				sbclsmstPosASEAN.downloadAmendedRecords(index);
			} else {
				sbclsmstPos.downloadAmendedRecords(index);
			}

		}

		// writeLog("Store " + ssStoreInfo.STORE() + " Sbclsmst");
	}

	public void deleteAmendedBarcode() throws Exception {
		if (STORELIST == null)
			STORELIST = new ArrayList();
		deleteAmended = new DeleteAmended(conn);
		PosXbarcodeDownload xbarcodePos = new PosXbarcodeDownload(conn, STORELIST);
		for (int i = 0; i < STORELIST.size(); i++) {
			xbarcodePos.deleteAmendedRecords(i, deleteAmended);

			ssStoreInfo = (StrmstInfo) STORELIST.get(i);
			// writeLog("Delete Store " + ssStoreInfo.STORE() + " Xbarcode");
		}
	}

	/* Added by Khim 20091224 - without loop */
	private void callCreateBarcode(char flag, int iLoop) throws Exception {
		// JoeMy 2014-08-13 WINCOR PCR
		if (ssStoreInfo.STORE_POS_TYPE() != null
				&& ssStoreInfo.STORE_POS_TYPE().equals(PosFileInfo.SYSWincorPOS)) {
			if (flag == PosFileInfo.WHOLE) {
				wincorXbarcode.downloadWholeTable(iLoop);
			} else {
				wincorXbarcode.downloadAmendedRecords(iLoop);
			}
		} else {
			if (flag == PosFileInfo.WHOLE) {
				xbarcodePos.downloadWholeTable(iLoop);
			} else {
				xbarcodePos.downloadAmendedRecords(iLoop);
			}
		}

	}

	public void callCreateBarcode(char flag) throws Exception {
		if (STORELIST == null)
			STORELIST = new ArrayList();
		PosXbarcodeDownload xbarcodePos = new PosXbarcodeDownload(conn, STORELIST);
		WincorXbarcodeDownload wincorXbarcode = new WincorXbarcodeDownload(conn, STORELIST);

		for (int i = 0; i < STORELIST.size(); i++) {
			// JoeMy 2014-08-13 WINCOR PCR
			ssStoreInfo = (StrmstInfo) STORELIST.get(i);
			if (ssStoreInfo.STORE_POS_TYPE() != null
					&& ssStoreInfo.STORE_POS_TYPE().equals(PosFileInfo.SYSWincorPOS)) {
				if (flag == PosFileInfo.WHOLE) {
					wincorXbarcode.downloadWholeTable(i);
				} else {
					wincorXbarcode.downloadAmendedRecords(i);
				}
			} else {
				if (flag == PosFileInfo.WHOLE) {
					xbarcodePos.downloadWholeTable(i);
				} else {
					xbarcodePos.downloadAmendedRecords(i);
					// writeLog("Store " + ssStoreInfo.STORE() + " Xbarcode");
				}
			}
		}

	}

	public void deleteAmendedItem() throws Exception {
		if (STORELIST == null)
			STORELIST = new ArrayList();
		deleteAmended = new DeleteAmended(conn);
		PosItemmstDownload itemPos = new PosItemmstDownload(conn, STORELIST);
		PosItemmstDownloadASEAN itemPosASEAN = new PosItemmstDownloadASEAN(conn, STORELIST);
		PosItemmstDownloadASEANAIN itemPosASEANAIN = new PosItemmstDownloadASEANAIN(conn, STORELIST); // SYCHUA 2015-03-16 - AIN PCR

		for (int i = 0; i < STORELIST.size(); i++) {
			ssStoreInfo = (StrmstInfo) STORELIST.get(i);
			// JoeMy 2013-09-17 ASEAN PCR
			if (PosFileInfo.SYSPosMethod.equals(PosFileInfo.PosASEAN)) {
				if (PosFileInfo.SYSPosMethodAsean.equals(PosFileInfo.PosASEANAIN)) // SYCHUA 2015-03-16 - AIN PCR
					itemPosASEANAIN.deleteAmendedRecords(i, deleteAmended);
				else
					itemPosASEAN.deleteAmendedRecords(i, deleteAmended);
			} else {
				itemPos.deleteAmendedRecords(i, deleteAmended);
			}
			// writeLog("Delete Store " + ssStoreInfo.STORE() + " Itemmst");
		}
	}

	/* Added by Khim 20091224 - without loop */
	private void callCreateItem(char flag, int iLoop) throws Exception {
		// JoeMy 2014-08-13 WINCOR PCR
		if (ssStoreInfo.STORE_POS_TYPE() != null
				&& ssStoreInfo.STORE_POS_TYPE().equals(PosFileInfo.SYSWincorPOS)) {
			if (flag == PosFileInfo.WHOLE) {
				wincorItem.downloadWholeTable(iLoop);
			} else {
				wincorItem.downloadAmendedRecords(iLoop);

			}
		} else {
			// JoeMy 2013-09-17 ASEAN PCR
			if (PosFileInfo.SYSPosMethod.equals(PosFileInfo.PosASEAN)) {
				if (PosFileInfo.SYSPosMethodAsean.equals(PosFileInfo.PosASEANAIN)) // SYCHUA 2015-03-16 - AIN PCR
				{
					if (flag == PosFileInfo.WHOLE) {
						itemPosASEANAIN.downloadWholeTable(iLoop);
					} else {
						itemPosASEANAIN.downloadAmendedRecords(iLoop);
					}
				} else {
					if (flag == PosFileInfo.WHOLE) {
						itemPosASEAN.downloadWholeTable(iLoop);
					} else {
						itemPosASEAN.downloadAmendedRecords(iLoop);
					}
				}
			} else {
				if (flag == PosFileInfo.WHOLE) {
					itemPos.downloadWholeTable(iLoop);
				} else {
					itemPos.downloadAmendedRecords(iLoop);

				}
			}
		}
	}

	public void callCreateItem(char flag) throws Exception {
		if (STORELIST == null)
			STORELIST = new ArrayList();
		PosItemmstDownload itemPos = new PosItemmstDownload(conn, STORELIST);
		PosItemmstDownloadASEAN itemPosASEAN = new PosItemmstDownloadASEAN(conn, STORELIST);
		PosItemmstDownloadASEANAIN itemPosASEANAIN = new PosItemmstDownloadASEANAIN(conn, STORELIST); // SYCHUA 2015-03-16 - AIN PCR
		WincorItemmstDownload wincorItem = new WincorItemmstDownload(conn, STORELIST);

		for (int i = 0; i < STORELIST.size(); i++) {
			// JoeMy 2014-08-13 WINCOR PCR
			ssStoreInfo = (StrmstInfo) STORELIST.get(i);
			if (ssStoreInfo.STORE_POS_TYPE() != null
					&& ssStoreInfo.STORE_POS_TYPE().equals(PosFileInfo.SYSWincorPOS)) {
				if (flag == PosFileInfo.WHOLE) {
					wincorItem.downloadWholeTable(i);
				} else {
					// ssStoreInfo = (StrmstInfo) STORELIST.get(i);
					wincorItem.downloadAmendedRecords(i);
				}
			} else {
				// JoeMy 2013-09-17 ASEAN PCR
				if (PosFileInfo.SYSPosMethod.equals(PosFileInfo.PosASEAN)) {
					if (PosFileInfo.SYSPosMethodAsean.equals(PosFileInfo.PosASEANAIN)) // SYCHUA 2015-03-16 - AIN PCR
					{
						if (flag == PosFileInfo.WHOLE) {
							itemPosASEANAIN.downloadWholeTable(i);
						} else {
							// ssStoreInfo = (StrmstInfo) STORELIST.get(i);
							itemPosASEANAIN.downloadAmendedRecords(i);
							// writeLog("Store " + ssStoreInfo.STORE() + " Itemmst");
						}
					} else {
						if (flag == PosFileInfo.WHOLE) {
							itemPosASEAN.downloadWholeTable(i);
						} else {
							// ssStoreInfo = (StrmstInfo) STORELIST.get(i);
							itemPosASEAN.downloadAmendedRecords(i);
							// writeLog("Store " + ssStoreInfo.STORE() + " Itemmst");
						}
					}
				} else {
					if (flag == PosFileInfo.WHOLE) {
						itemPos.downloadWholeTable(i);
					} else {
						// ssStoreInfo = (StrmstInfo) STORELIST.get(i);
						itemPos.downloadAmendedRecords(i);
						// writeLog("Store " + ssStoreInfo.STORE() + " Itemmst");
					}
				}
			}

		}

	}

	public void deleteAmendedPromotion() throws Exception {
		if (STORELIST == null)
			STORELIST = new ArrayList();
		deleteAmended = new DeleteAmended(conn);
		PosPrcChgDownload prcChg = new PosPrcChgDownload(conn, STORELIST);
		PosMixmPmtDownload mixmPmt = new PosMixmPmtDownload(conn, STORELIST);
		PosMixmComDownload mixmCom = new PosMixmComDownload(conn, STORELIST);
		PosPrcChgDownloadASEAN prcChgASEAN = new PosPrcChgDownloadASEAN(conn, STORELIST);
		PosMixmPmtDownloadASEAN mixmPmtASEAN = new PosMixmPmtDownloadASEAN(conn, STORELIST);
		PosMixmComDownloadASEAN mixmComASEAN = new PosMixmComDownloadASEAN(conn, STORELIST);

		for (int i = 0; i < STORELIST.size(); i++) {
			ssStoreInfo = (StrmstInfo) STORELIST.get(i);
			// JoeMy 2013-09-12 ASEAN PCR
			if (PosFileInfo.SYSPosMethod.equals(PosFileInfo.PosASEAN)) {
				prcChgASEAN.deleteAmendedRecords(i, deleteAmended);
				// writeLog("Delete Store " + ssStoreInfo.STORE() + " Prchghdr");
				mixmPmtASEAN.deleteAmendedRecords(i, deleteAmended);
				// writeLog("Delete Store " + ssStoreInfo.STORE() + " MixmPmt");
				mixmComASEAN.deleteAmendedRecords(i, deleteAmended);
				// writeLog("Delete Store " + ssStoreInfo.STORE() + " MixmCom");
			} else {
				prcChg.deleteAmendedRecords(i, deleteAmended);
				// writeLog("Delete Store " + ssStoreInfo.STORE() + " Prchghdr");
				mixmPmt.deleteAmendedRecords(i, deleteAmended);
				// writeLog("Delete Store " + ssStoreInfo.STORE() + " MixmPmt");
				mixmCom.deleteAmendedRecords(i, deleteAmended);
				// writeLog("Delete Store " + ssStoreInfo.STORE() + " MixmCom");
			}

		}
	}

	/* Added by Khim 20091224 - without loop */
	private void callCreatePromotion(char flag, int iLoop) throws Exception {

		// Joan 2014-08-20 WINCOR PCR
		if (ssStoreInfo.STORE_POS_TYPE() != null
				&& ssStoreInfo.STORE_POS_TYPE().equals(PosFileInfo.SYSWincorPOS)) {
			if (flag == PosFileInfo.WHOLE) {
				this.wincortplgqmst.downloadWholeTable(iLoop);
				this.wincortplpromo.downloadWholeTable(iLoop);
				this.wincortplrulehdr.downloadWholeTable(iLoop);
				this.wincortplruledetline.downloadWholeTable(iLoop);
				this.wincortplruledetquantity.downloadWholeTable(iLoop);
				this.wincortplruledetcombination.downloadWholeTable(iLoop);
				this.wincortplliqhdr.downloadWholeTable(iLoop);
				this.wincortplliqdet.downloadWholeTable(iLoop);
				this.wincortpdisclin.downloadWholeTable(iLoop);
			} else {
				this.wincortplgqmst.downloadAmendedRecords(iLoop);
				this.wincortplpromo.downloadAmendedRecords(iLoop);
				this.wincortplrulehdr.downloadAmendedRecords(iLoop);
				this.wincortplruledetline.downloadAmendedRecords(iLoop);
				this.wincortplruledetquantity.downloadAmendedRecords(iLoop);
				this.wincortplruledetcombination.downloadAmendedRecords(iLoop);
				this.wincortplliqhdr.downloadAmendedRecords(iLoop);
				this.wincortplliqdet.downloadAmendedRecords(iLoop);
				this.wincortpdisclin.downloadAmendedRecords(iLoop);

			}
		} else {
			// JoeMy 2013-09-12 ASEAN PCR
			if (PosFileInfo.SYSPosMethod.equals(PosFileInfo.PosASEAN)) {
				if (flag == PosFileInfo.WHOLE) {
					prcChgASEAN.downloadWholeTable(iLoop);
					mixmPmtASEAN.downloadWholeTable(iLoop);
					mixmComASEAN.downloadWholeTable(iLoop);
				} else {
					prcChgASEAN.downloadAmendedRecords(iLoop);
					mixmPmtASEAN.downloadAmendedRecords(iLoop);
					mixmComASEAN.downloadAmendedRecords(iLoop);
				}
			} else {
				if (flag == PosFileInfo.WHOLE) {
					prcChg.downloadWholeTable(iLoop);
					mixmPmt.downloadWholeTable(iLoop);
					mixmCom.downloadWholeTable(iLoop);
				} else {
					prcChg.downloadAmendedRecords(iLoop);
					mixmPmt.downloadAmendedRecords(iLoop);
					mixmCom.downloadAmendedRecords(iLoop);
				}
			}
		}
	}

	public void callCreatePromotion(char flag) throws Exception {
		if (STORELIST == null)
			STORELIST = new ArrayList();
		PosPrcChgDownload prcChg = new PosPrcChgDownload(conn, STORELIST);
		PosMixmPmtDownload mixmPmt = new PosMixmPmtDownload(conn, STORELIST);
		PosMixmComDownload mixmCom = new PosMixmComDownload(conn, STORELIST);
		PosPrcChgDownloadASEAN prcChgASEAN = new PosPrcChgDownloadASEAN(conn, STORELIST);
		PosMixmPmtDownloadASEAN mixmPmtASEAN = new PosMixmPmtDownloadASEAN(conn, STORELIST);
		PosMixmComDownloadASEAN mixmComASEAN = new PosMixmComDownloadASEAN(conn, STORELIST);

		// Joan 2014-08-20 WINCOR PCR
		WincorTplGqmst wincortplgqmst = new WincorTplGqmst(this.conn, this.STORELIST);
		WincorTplPromo wincortplpromo = new WincorTplPromo(this.conn, this.STORELIST);
		WincorTplRuleHdr wincortplrulehdr = new WincorTplRuleHdr(this.conn, this.STORELIST);
		WincorTplRuleDetLine wincortplruledetline = new WincorTplRuleDetLine(this.conn, this.STORELIST);
		WincorTplRuleDetQuantity wincortplruledetquantity = new WincorTplRuleDetQuantity(this.conn, this.STORELIST);
		WincorTplRuleDetCombination wincortplruledetcombination = new WincorTplRuleDetCombination(this.conn, this.STORELIST);
		WincorTplLiqHdr wincortplliqhdr = new WincorTplLiqHdr(this.conn, this.STORELIST);
		WincorTplLiqDet wincortplliqdet = new WincorTplLiqDet(this.conn, this.STORELIST);
		WincorTplDiscLin wincortpdisclin = new WincorTplDiscLin(this.conn, this.STORELIST);

		profitvvManager = new ProfitvvManager(); // Added by kofam 2017-03-30 AVN - Interface with E-Commerce
		setProcessType(false); // Added by kofam 2017-03-30 AVN - Interface with E-Commerce

		for (int i = 0; i < STORELIST.size(); i++) {
			// Joan 2014-08-20 WINCOR PCR
			ssStoreInfo = (StrmstInfo) STORELIST.get(i);
			if (ssStoreInfo.STORE_POS_TYPE() != null
					&& ssStoreInfo.STORE_POS_TYPE().equals(PosFileInfo.SYSWincorPOS)) {
				if (flag == PosFileInfo.WHOLE) {
					wincortplgqmst.downloadWholeTable(i);
					wincortplpromo.downloadWholeTable(i);
					wincortplrulehdr.downloadWholeTable(i);
					wincortplruledetline.downloadWholeTable(i);
					wincortplruledetquantity.downloadWholeTable(i);
					wincortplruledetcombination.downloadWholeTable(i);
					wincortplliqhdr.downloadWholeTable(i);
					wincortplliqdet.downloadWholeTable(i);
					wincortpdisclin.downloadWholeTable(i);

				} else {
					wincortplgqmst.downloadAmendedRecords(i);
					wincortplpromo.downloadAmendedRecords(i);
					wincortplrulehdr.downloadAmendedRecords(i);
					wincortplruledetline.downloadAmendedRecords(i);
					wincortplruledetquantity.downloadAmendedRecords(i);
					wincortplruledetcombination.downloadAmendedRecords(i);
					wincortplliqhdr.downloadAmendedRecords(i);
					wincortplliqdet.downloadAmendedRecords(i);
					wincortpdisclin.downloadAmendedRecords(i);
				}
			} else {
				// JoeMy 2013-09-12 ASEAN PCR
				if (PosFileInfo.SYSPosMethod.equals(PosFileInfo.PosASEAN)) {
					if (flag == PosFileInfo.WHOLE) {
						if (profitvvManager.chkProfitvvKey(PosFileInfo.SYSEcomDwnStore, ssStoreInfo.STORE())
								|| PosFileInfo.SYSEcomDwnStore.equals("All"))
							setProcessType(true);

						prcChgASEAN.setEcommType(Dwn_Type, bPromoType);
						prcChgASEAN.downloadWholeTable(i);
						// Edited by kofam 2017-03-30 AVN - Interface with
						// E-Commerce
						if (bPosDwn) {
							setProcessType(false);
							mixmPmtASEAN.downloadWholeTable(i);
							mixmComASEAN.downloadWholeTable(i);
						}
						// End edited by kofam 2017-03-30
					} else {
						if (profitvvManager.chkProfitvvKey(PosFileInfo.SYSEcomDwnStore, ssStoreInfo.STORE())
								|| PosFileInfo.SYSEcomDwnStore.equals("All"))
							setProcessType(true);

						ssStoreInfo = (StrmstInfo) STORELIST.get(i);
						prcChgASEAN.setEcommType(Dwn_Type, bPromoType);
						prcChgASEAN.downloadAmendedRecords(i);
						// writeLog("Store " + ssStoreInfo.STORE() + " Prchghdr");
						// Edited by kofam 2017-03-30 AVN - Interface with E-Commerce
						if (bPosDwn) {
							setProcessType(false);
							mixmPmtASEAN.downloadAmendedRecords(i);
							// writeLog("Store " + ssStoreInfo.STORE() + " MixmPmt");
							mixmComASEAN.downloadAmendedRecords(i);
							// writeLog("Store " + ssStoreInfo.STORE() + " MixmCom");
						}
						// End edited by kofam 2017-03-30
					}
				} else {
					if (flag == PosFileInfo.WHOLE) {
						prcChg.downloadWholeTable(i);
						mixmPmt.downloadWholeTable(i);
						mixmCom.downloadWholeTable(i);
					} else {
						ssStoreInfo = (StrmstInfo) STORELIST.get(i);
						prcChg.downloadAmendedRecords(i);
						// writeLog("Store " + ssStoreInfo.STORE() + " Prchghdr");
						mixmPmt.downloadAmendedRecords(i);
						// writeLog("Store " + ssStoreInfo.STORE() + " MixmPmt");
						mixmCom.downloadAmendedRecords(i);
						// writeLog("Store " + ssStoreInfo.STORE() + " MixmCom");
					}
				}

			}

		}

		setProcessType(false); // Added by kofam 2017-03-30 AVN - Interface with E-Commerce
		/*
		 * PosPromoStrsendDownload posPromoDwn = new
		 * PosPromoStrsendDownload(conn); PosPMTDownloadProp pmtProp = new
		 * PosPMTDownloadProp(conn); PosCOMDownloadProp comProp = new
		 * PosCOMDownloadProp(conn);
		 * 
		 * posPromoDwn.setProperties(pmtProp, comProp);
		 * 
		 * for(int i=0; i<PosFileInfo.STORELIST.size(); i++) {
		 * if(flag==PosFileInfo.WHOLE) { posPromoDwn.downloadWholeTable(i); }
		 * else { posPromoDwn.downloadStrsendTable(i);
		 * posPromoDwn.clearStrsendPrcMixm(); } }
		 */
	}

	public void deleteAmendedGrpPrcChg() throws Exception {
		if (STORELIST == null)
			STORELIST = new ArrayList();
		deleteAmended = new DeleteAmended(conn);
		PosGrpPrcChgDownload grpPrcChg = new PosGrpPrcChgDownload(conn, STORELIST);

		for (int i = 0; i < STORELIST.size(); i++) {
			ssStoreInfo = (StrmstInfo) STORELIST.get(i);
			grpPrcChg.deleteAmendedRecords(i, deleteAmended);
			// writeLog("Delete Store " + ssStoreInfo.STORE() + " Grprchghdr");
		}
	}

	/* Added by Khim 20091224 - without loop */
	private void callCreateGrpPrcChg(char flag, int iLoop) throws Exception {

		if (flag == PosFileInfo.WHOLE) {
			grpPrcChg.downloadWholeTable(iLoop);
		} else {
			grpPrcChg.downloadAmendedRecords(iLoop);
		}
	}

	public void callCreateGrpPrcChg(char flag) throws Exception {
		if (STORELIST == null)
			STORELIST = new ArrayList();
		PosGrpPrcChgDownload grpPrcChg = new PosGrpPrcChgDownload(conn, STORELIST);

		for (int i = 0; i < STORELIST.size(); i++) {
			if (flag == PosFileInfo.WHOLE) {
				grpPrcChg.downloadWholeTable(i);
			} else {
				ssStoreInfo = (StrmstInfo) STORELIST.get(i);
				grpPrcChg.downloadAmendedRecords(i);
				// writeLog("Store " + ssStoreInfo.STORE() + " Grprchghdr");
			}
		}

	}

	// Added by kofam 2017-03-30 AVN - Interface with E-Commerce
	public void setEcommFlag(boolean processEcomm, boolean processEcommWhole,
			boolean processPOS) throws Exception {
		if (processEcomm) {
			if (processEcommWhole)
				Dwn_Type = "EcommWhole";
			else
				Dwn_Type = "Ecomm";
		}

		bPosDwn = processPOS;
	}

	/** To determine it is running from Promotion process or not **/
	private void setProcessType(boolean processType) throws Exception {
		this.bPromoType = processType;
	}

	// End added by kofam 2017-03-30

	public void run(HParam hParam) throws Exception {
		System.out.println("run from background");
		String filename = "/tmp/.posdwncontrolfile";

		HParam param = new HParam();

		param.put("cboStore", "");
		param.put("cboCOY_SUB", "All");
		param.put("COY", hParam.getString("COY").trim());

		// Added by khim 20121018 (Ref: AU-SD003858559 (AU-IM001013432))
		param.put("EODQUEUE_COY", hParam.getString("EODQUEUE_COY"));
		param.put("EODQUEUE_COY_SUB", hParam.getString("EODQUEUE_COY_SUB"));
		param.put("EODQUEUE_STORE", hParam.getString("EODQUEUE_STORE").trim());
		param.put("EODQUEUE_QUEUE_ID", hParam.getString("EODQUEUE_QUEUE_ID").trim());
		param.put("EODQUEUE_PROG_ID", hParam.getString("EODQUEUE_PROG_ID").trim());
		param.put("EODQUEUE_SEQ_NO", hParam.getString("EODQUEUE_SEQ_NO").trim());
		param.put("EODQUEUE_TRANS_DATE", hParam.getString("EODQUEUE_TRANS_DATE").trim());
		param.put("BACKEND", hParam.getString("BACKEND").trim()); // BACKEND = Y - from EOD batch.EOD.EODController

		param.put("chkTableType1", "on");
		param.put("chkTableType1_1", "on");

		param.put("chkTableType2", "on");
		param.put("chkTableType2_1", "on");

		param.put("chkTableType3", "on");
		param.put("chkTableType3_1", "on");

		param.put("chkTableType4", "on");
		param.put("chkTableType4_1", "on");

		param.put("chkTableType5", "on");
		param.put("chkTableType5_1", "on");

		ProfitvvSQL profitvvSQL = new ProfitvvSQL();
		profitvvSQL.setCOY(hParam.getString("COY").trim());
		profitvvSQL.setVNM("SYSGenerator");
		profitvvSQL.getByKey();

		AduserSQL aduserSQL = new AduserSQL();
		aduserSQL.setUSR_ID(profitvvSQL.VNM_VDTVL());
		aduserSQL.getByKey();

		Thread process = new PosDwnProcess(param, filename, profitvvSQL.VNM_VDTVL(), aduserSQL.USR_LANGUAGE());
		process.setPriority(Thread.MAX_PRIORITY);
		process.start();
	}

	public String isProcessRunning() throws Exception {
		String strLastOpr = "";

		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT LAST_OPR FROM LOCKERMST WHERE LOCK_TABLE = 'POS_DOWNLOAD_DATA'");

		if (rs != null && rs.next()) {
			strLastOpr = rs.getString("LAST_OPR");
		}

		return strLastOpr;
	}

	/* Added by Khim 20100104 */
	public void insertLock(String strCurrDate, String strOpr) throws Exception {
		LockermstSQL lockermstSQL = new LockermstSQL(conn_autoCommit);
		lockermstSQL.setLOCK_TABLE("POS_DOWNLOAD_DATA");
		lockermstSQL.setLOCK_KEY1(strCurrDate);
		lockermstSQL.setLOCK_REMARK("EOD_POS_DOWNLOAD_DATA");
		lockermstSQL.setLAST_OPR(strOpr);
		lockermstSQL.insert();

	}

	/* Added by Khim 20100104 */
	public void removeLock(String strCurrDate) throws Exception {
		LockermstSQL lockermstSQL = new LockermstSQL(conn_autoCommit);
		lockermstSQL.setLOCK_TABLE("POS_DOWNLOAD_DATA");
		lockermstSQL.setLOCK_KEY1(strCurrDate);
		lockermstSQL.delete();

	}

	public String getCurrDate() throws Exception {
		SysctlSQL sysctl = new SysctlSQL(conn);

		return qrMisc.formatDate(sysctl.getLAST_DAY_UPDATEDPlusOne());
	}

	public String getStoreValue(String strCoy) throws Exception {
		String strStore = null;

		ProfitvvSQL profitvvSQL = new ProfitvvSQL(conn);
		profitvvSQL.setCOY(strCoy);
		profitvvSQL.setVNM("SYSHeadOfficeStore");

		if (profitvvSQL.getByKey() > 0) {
			strStore = profitvvSQL.VNM_VDTVL();
		} else {
			strStore = "";
		}

		return strStore;
	}

	// JoeMy 2014-08-27 WINCOR PCR, same timestamp in one batch
	private String getFilename() throws IOException {
		Calendar cal = Calendar.getInstance();
		String YEAR = String.valueOf(cal.get(Calendar.YEAR));
		String DAY = qrMisc.leftFillZero(String.valueOf(cal.get(Calendar.DATE)), 2);
		String TIME = qrMisc.leftFillZero(
				String.valueOf(cal.get(Calendar.HOUR_OF_DAY)), 2)
				+ qrMisc.leftFillZero(String.valueOf(cal.get(Calendar.MINUTE)), 2)
				+ qrMisc.leftFillZero(String.valueOf(cal.get(Calendar.SECOND)), 2);

		int month = cal.get(Calendar.MONTH) + 1;
		String MONTH = qrMisc.leftFillZero(String.valueOf(month), 2);

		StringBuffer buf = new StringBuffer();

		buf.append(YEAR);
		buf.append(MONTH);
		buf.append(DAY);
		buf.append(TIME);

		return buf.toString();
	}

	// JoeMy 2014-08-27 WINCOR PCR, same timestamp in one batch
	public void moveBingo2RealPath() throws Exception {
		if (STORELIST == null)
			STORELIST = new ArrayList();
		File outputFile;
		File realFile;

		for (int i = 0; i < STORELIST.size(); i++) {
			ssStoreInfo = (StrmstInfo) STORELIST.get(i);

			if (ssStoreInfo.STORE_POS_TYPE() != null
					&& ssStoreInfo.STORE_POS_TYPE().equals(PosFileInfo.SYSWincorPOS)) {
				outputFile = new File(PosFileInfo.PATH_2 + "/"
						+ ssStoreInfo.STORE() + "/bingo/" + "bingo."
						+ ssStoreInfo.STORE() + "." + PosFileInfo.FILENAME);
				if (outputFile.exists() && !outputFile.isDirectory()) // Move files if only exists, because some store  might not have .iu or .d file, so no bingo will generated for that store
				{
					realFile = new File(PosFileInfo.PATH_2 + "/" + ssStoreInfo.STORE() + "/" + outputFile.getName());

					outputFile.renameTo(realFile);
					chmod(realFile.getAbsolutePath());

				}
				outputFile = null;
				realFile = null;
			}
		}

	}

	private void chmod(String absolutePath) {
		String OS = System.getProperty("os.name").toUpperCase();

		if (!OS.startsWith("WIN")) {
			Process proc = null;
			BufferedReader buf = null;
			InputStreamReader reader = null;
			try {
				String cmd;
				if (OS.startsWith("LIN"))
					cmd = "/bin/chmod 777 " + absolutePath;
				else
					cmd = "/usr/bin/chmod -h 777 " + absolutePath;
				// System.out.println(cmd);
				proc = Runtime.getRuntime().exec(cmd);
				reader = new InputStreamReader(proc.getErrorStream());
				buf = new BufferedReader(reader);
				// String c = null;
				// while((c = buf.readLine())!=null)
				// System.out.println(c);
				// System.out.println(proc.waitFor());
			} catch (Exception e) {
				System.out.println("chmod error");
				e.printStackTrace();
			} finally {
				try {
					if (buf != null)
						buf.close();
				} catch (Exception e) {
				}
				buf = null;
				try {
					if (reader != null)
						reader.close();
				} catch (Exception e) {
				}
				reader = null;
			}
		}
	}

	private void openConnection() {
		try {
			conn = DataSource.getLocalConnection();
			conn.setAutoCommit(false);

			conn_autoCommit = DataSource.getLocalConnection();
		} catch (Exception sqle) {
		}
	}

	public void closeConnection() {
		try {
			if (conn != null)
				conn.close();

			if (conn_autoCommit != null)
				conn_autoCommit.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		conn = null;
	}

	public static void main(String[] args) throws Exception {
		HParam param = new HParam();
		String filename = "/tmp/.posdwncontrolfile";
		/*
		 * if(args.length==0) { param.put("cboStore", "1002");
		 * param.put("cboCOY_SUB", "AMY"); param.put("COY", "AMY");
		 * 
		 * param.put("chkTableType1", "");//master param.put("chkTableType1_2",
		 * "");//amended only
		 * 
		 * param.put("chkTableType2", "on");//item param.put("chkTableType2_2",
		 * "on");//amended only
		 * 
		 * param.put("chkTableType3", "");//barcode param.put("chkTableType3_1",
		 * "");//amended only
		 * 
		 * param.put("chkTableType4", "");//promotion
		 * param.put("chkTableType4_2", "");//amended only
		 * 
		 * param.put("chkTableType5", "");//group prc chg
		 * param.put("chkTableType5_1", "");//amended only }
		 */

		if (args.length != 4) {
			System.out.println("USAGE: ");
			System.out.println("java -cp .:/oracle/App/j2ee/home/oc4j.jar qrcom.PROFIT.files.strsend.pos.DownloadCtr {COY} {COY_SUB} {STORE} {DOWNLOAD_MODE 0|1}");

			System.exit(0);
		} else {
			String download_mode = args[3];

			param.put("COY", args[0]);
			param.put("cboCOY_SUB", args[1]);
			param.put("cboStore", args[2]);

			if (download_mode.equals("0")) // whole download
			{
				param.put("chkTableType1", "on");// master
				param.put("chkTableType1_2", "on");// all

				param.put("chkTableType2", "on");// item
				param.put("chkTableType2_2", "on");// all

				param.put("chkTableType3", "on");// barcode
				param.put("chkTableType3_2", "on");// all

				param.put("chkTableType4", "on");// promotion
				param.put("chkTableType4_2", "on");// all

				param.put("chkTableType5", "on");// group prc chg
				param.put("chkTableType5_2", "on");// all
			} else if (download_mode.equals("1")) // amended only download
			{
				param.put("chkTableType1", "on");// master
				param.put("chkTableType1_1", "on");// amended only

				param.put("chkTableType2", "on");// item
				param.put("chkTableType2_1", "on");// amended only

				param.put("chkTableType3", "on");// barcode
				param.put("chkTableType3_1", "on");// amended only

				param.put("chkTableType4", "on");// promotion
				param.put("chkTableType4_1", "on");// amended only

				param.put("chkTableType5", "on");// group prc chg
				param.put("chkTableType5_1", "on");// amended only
			}

		}

		Thread process = new PosDwnProcess(param, filename, "SYS", "0");
		process.setPriority(Thread.MAX_PRIORITY);
		process.start();
	}
}