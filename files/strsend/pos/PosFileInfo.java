// changes
// guoliang 2014-05-21 AU-IM002701122 reenabled static var COY_SUB for use by PosItemmstDownload and PosItemmstDownloadASEAN
// JoeMy 2014-08-13 WINCOR PCR
// JoanChia 2014-08-20 WINCOR PCR (promotion) 
// JoeMy 2015-01-12 POS DOS and Q5 PCR 

package qrcom.PROFIT.files.strsend.pos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import qrcom.PROFIT.files.info.AltDescUtil;
import qrcom.PROFIT.files.info.StrmstInfo;
import qrcom.PROFIT.files.info.StrposdlseqSQL;
import qrcom.util.qrMath;
//import qrcom.PROFIT.system.SysParam;
import qrcom.util.qrMisc;

public abstract class PosFileInfo {
	public ArrayList STORELIST;
	// public static boolean ALL = true;

	public static String COY;
	public static String COY_SUB;
	public static String PATH;
	public static String PATH_2;
	public static String PATH_Q5;
	public static String PATH_ECOMM; // Added by kofam 2017-03-30 AVN - Interface with E-Commerce
	public static String LANGCODE;
	public static String SYSBarConsNPLUPrefix;
	public static String SYSBarNonPLUPrefix;
	public static String SYSPosMethod;
	public static String SYSPosMethodAsean; // SYCHUA 2015-03-16 - AIN PCR
	public static String SYSMultiInHouseBar;
	public static String SYSBarNormNPLUPrefix;
	public static String SYSPosCtrlItem;
	public static String SYSEnbWincorPOSBck;
	public static String SYSEnbWincorPOSBingo;
	public static String SYSWrtWincorPOSBingo;
	public static String SYSEcomDownload; // Added by kofam 2017-03-30 AVN - Interface with E-Commerce
	public static String SYSEcomDwnBackup; // Added by kofam 2017-03-30 AVN - Interface with E-Commerce
	public static String SYSEcomDwnStore; // Added by kofam 2017-03-30 AVN - Interface with E-Commerce

	public static final String SEPARATOR = ";";

	// Joan 2014-08-20 WINCOR PCR
	public static String SYSPmgrpLength;
	public static String SYSDivLength;
	public static String SYSDeptLength;
	public static String SYSSkuMax;
	public static String SYSClassLength;
	public static String SYSSbClsLength;
	public static String SYSSubCatLength;
	public static String SYSBarcodeLength;
	public static String SYSTPLDwnMaxAmt;
	public static String SYSTPLDwnMaxQty;

	public static String SYSSeitoPOS; // Seito DOS POS
	public static String SYSWincorPOS; // WINCOR POS
	public static String SYSSeitoPOSQ5; // Seito Q5 POS

	public static String SYSSellVATBefGST;
	public static String SYSDwnDefSellVat;

        protected static String SYSSalesTaxCode;

	public static String SYSPosBarFillMethod; // SYCHUA 2015-04-26 - AIN PCR

	public static String SYSAddAutoDiscFlg; // ALANHENG 2017-07-24 - SRAM067156 Additional Auto Discount System Enhancement

	public static String FILENAME;

	public static final String DIVISION = "D";// SECTION
	public static final String DEPARTMENT = "T";// TYPE
	public static final String CLASS = "A";// ARTICLE
	public static final String SUBCLASS = "C";// CLASS
	public static final String ITEMMST = "I";// ITEMMST
	public static final String BARCODE = "B";// BARCODE
	public static final String PRC_CHG = "P";// PRICE CHANGE
	public static final String MIXM_PMT = "M";// MIX AND MATCH (QUANTITY & PARTIAL)
	public static final String MIXM_COM = "N";// MIX AND MATCH (COMBINATION)
	public static final String GRP_PRC_CHG = "G";// GROUP PRICE CHANGE

	public static final String WINCOR_MASTER = "dept"; // MASTER HIERARCHY
	public static final String WINCOR_MASTER_PLU = "pluext"; // MASTER HIERARCHY in PLU (CLASS, SBCLS, SBCAT)
	public static final String WINCOR_ITEM = "plu"; // ITEM
	public static final String WINCOR_BARCODE = "sku"; // BARCODE

	public static final String WINCOR_PROMOTION_TIME_DEF = "disc_pro"; // promotion time definition
	public static final String WINCOR_DISCOUNT_RULE = "disc_rul";// discount rule
	public static final String WINCOR_QUANTITY_DISCOUNT = "disc_rulqd";// quantity discount
	public static final String WINCOR_LINE_DISCOUNT = "disc_rulld";// line discount
	public static final String WINCOR_COMBINATION_DISCOUNT = "disc_rulcd";// combination discount
	public static final String WINCOR_ITEM_QUALIFIER = "disc_iq";// item qualifier
	public static final String WINCOR_ITEM_QUALIFIER_DETAIL = "disc_iqd";// item qualifier detail
	public static final String WINCOR_DISCOUNT_LINKING = "disc_lin";// discount linking
	public static final String WINCOR_GROUP_QUALIFIER = "disc_gq";// grp qualifier

	public static final String PosAMY = "1";
	public static final String PosASEAN = "2";

	public static final String PosASEANAIN = "2"; // SYCHUA 2015-03-16 - AIN PCR

	public static final char WHOLE = 'w';
	public static final char AMENDED = 'a';

	private ResultSet rs = null;
	private PreparedStatement pstmt = null;
	protected Connection conn = null;

	protected int index;
	protected String query;
	protected StringBuffer bufStr;
	protected StrmstInfo strmstInfo;

	protected boolean amended = true;
	protected PreparedStatement delPstmt = null;
	// protected String delQuery = "DELETE FROM STRSEND WHERE SS_TRANS_DATE = ? AND SS_TRANS_SEQ = ?";

	public final WriteOutput WRITER = new WriteOutput();
	public final WincorWriteOutput WINCOR_WRITER = new WincorWriteOutput();
	public final StrposdlseqSQL seqSQL = new StrposdlseqSQL();
	private SimpleDateFormat formatter = new SimpleDateFormat();

	protected void writeToFile() throws Exception {
		if (WRITER.writeToFile(bufStr.toString()) == false) {
			throw new Exception("Write to file failed");
		}
	}

	// JoeMy 2014-08-13 WINCOR PCR
	protected void writeToWincorFile() throws Exception {
		if (WINCOR_WRITER.writeToFile(bufStr.toString()) == false) {
			throw new Exception("[WINCOR] Write to file failed");
		}
	}

	protected void writeToWincorDelFile() throws Exception {
		if (WINCOR_WRITER.writeToDelFile(bufStr.toString()) == false) {
			throw new Exception("[WINCOR] Write to DEL file failed");
		}
	}

	/*
	 * protected void finish() throws Exception {
	 * if((index==(STORELIST.size()-1))) { if(PosFileInfo.ALL && amended &&
	 * delPstmt != null) { int[] rows = delPstmt.executeBatch();
	 * delPstmt.close(); delPstmt = null; } } }
	 */

	protected void execute() throws Exception {
		String q = getQuery();
		pstmt = conn.prepareStatement(q);
		bufStr = new StringBuffer();

		// JoeMy 2014-08-27 WINCOR PCR, same timestamp in one batch
		if (strmstInfo.STORE_POS_TYPE() != null
				&& strmstInfo.STORE_POS_TYPE().equals(PosFileInfo.SYSWincorPOS)) {
			WINCOR_WRITER.setFileName(strmstInfo.STORE() + "." + FILENAME);
		}

		rs = pstmt.executeQuery();
		processRS(rs);

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

		if (strmstInfo.STORE_POS_TYPE() != null
				&& strmstInfo.STORE_POS_TYPE().equals(PosFileInfo.SYSWincorPOS)) {
			WINCOR_WRITER.setBackupFlag(SYSEnbWincorPOSBck);
			WINCOR_WRITER.setBingoFlag(SYSEnbWincorPOSBingo);
			WINCOR_WRITER.setBingoWriteDataFlag(SYSWrtWincorPOSBingo);
			WINCOR_WRITER.closeFile();
		}
		// JoeMy 2015-01-12 POS DOS and Q5 PCR - start
		else if (strmstInfo.STORE_POS_TYPE() != null
				&& strmstInfo.STORE_POS_TYPE().equals(PosFileInfo.SYSSeitoPOSQ5)) {
			WRITER.setDOS_Q5_Flag("Y");
			WRITER.setStrList(STORELIST);
			WRITER.closeFile();
		}
		// JoeMy 2015-01-12 POS DOS and Q5 PCR - end
		else {
			WRITER.setDOS_Q5_Flag("N");
			WRITER.setStrList(STORELIST);
			WRITER.closeFile();
		}

		// this.finish();
	}

	/*
	 * protected void addDeleteRecords(ResultSet r) throws Exception {
	 * if(PosFileInfo.ALL && amended) { if(delPstmt == null) { delPstmt =
	 * conn.prepareStatement(delQuery); }
	 * 
	 * delPstmt.setDate(1, r.getDate("SS_TRANS_DATE")); delPstmt.setDouble(2,
	 * r.getDouble("SS_TRANS_SEQ")); delPstmt.addBatch(); } }
	 */

	protected String getShortSku(String sku) {
		if (sku == null || sku.trim().length() == 0) {
			return qrMisc.leftFillBlank("", 13);
		} else {
			String newSku = "0" + sku;
			return qrMisc.rightFillBlank(newSku, 13);
		}
	}

	protected String getBarcode(String bar) {
		/*
		 * remark by CHUA @ 2010-05-31 - As per feedback from SEITO, need to
		 * cater for AEON Msia old barcode, QR will not truncate the check
		 * digit. And if barcode's length < 13, will do left fill zero remark by
		 * CHUA @ 2010-06-17 - As per feedback from SEITO, for barcode start
		 * with 22 or 23 (Non PLU item), need to truncate check digit. Then will
		 * do left fill zero. Other than 22 or 23 will follow as current.
		 */
		if (SYSMultiInHouseBar.equals("Y")) {
			if (bar == null || bar.trim().length() == 0) {
				return qrMisc.rightFillZero("", 18);
			} else {
				int len = bar.length();

				if (len < 13 || len == 17) {
					if (SYSPosBarFillMethod.equals("1")) // sychua 2015-04-26 -
															// AIN PCR
						return qrMisc.rightFillZero(bar, 18);
					else
						return qrMisc.leftFillZero(bar, 18);
				} else {
					if (bar.substring(0, 2).equals(SYSBarConsNPLUPrefix)
							|| bar.substring(0, 2).equals(SYSBarNonPLUPrefix)
							|| bar.substring(0, 2).equals(SYSBarNormNPLUPrefix)) {
						bar = bar.substring(0, len - 1);

						if (SYSPosBarFillMethod.equals("1")) // sychua 2015-04-26 - AIN PCR
							bar = qrMisc.rightFillZero(bar, 18);
						else
							bar = qrMisc.leftFillZero(bar, 18);
					} else {
						if (SYSPosBarFillMethod.equals("1")) // sychua 2015-04-26 - AIN PCR
							bar = qrMisc.rightFillZero(bar, 18);
						else
							bar = qrMisc.leftFillZero(bar, 18);
					}

					return bar;
				}
			}
		} else {
			if (bar == null || bar.trim().length() == 0) {
				return qrMisc.leftFillZero("", 13);
			} else {
				int len = bar.length();

				if (len < 13)
					return qrMisc.leftFillZero(bar, 13);
				else {
					if (bar.substring(0, 2).equals(SYSBarConsNPLUPrefix)
							|| bar.substring(0, 2).equals(SYSBarNonPLUPrefix)) {
						bar = bar.substring(0, len - 1);
						bar = qrMisc.leftFillZero(bar, 13);
					}

					return bar;
				}
			}
		}

		// end add

		/*
		 * remark by CHUA @ 2010-05-31 - this method not using in AEON M'sia
		 * if(bar==null || bar.trim().length()==0) { return
		 * qrMisc.leftFillBlank("", 13); } else { String newBar; int len =
		 * bar.length(); if(len < 13) //len == 12. CHUA @ 2010-03-11 - change to
		 * check if len < 13, do left fill zero { newBar = bar.substring(0,
		 * len-1); //newBar = "0" + newBar; newBar = qrMisc.leftFillZero(newBar,
		 * 12); //always left fill zero up 12 digits return
		 * qrMisc.rightFillBlank(newBar, 13); } else { newBar = bar.substring(0,
		 * len-1); return qrMisc.rightFillBlank(newBar, 13); } }
		 */
	}

	// private String charset =
	// SysParam.getInstance().getResSystem("DBMS_encode_charset");
	// private String charset = "GBK";

	protected String getDesc(String lang, String desc, int len) {
		if (desc == null) {
			return qrMisc.rightFillBlank(" ", len);
		}

		String str = AltDescUtil.getDesc(lang, desc).trim();

		// int size = getSize(str);
		// System.out.println("text size 1 = " + size + " text size 2 = " +
		// textSize(str));
		int size = textSize(str);

		if (size > len) {
			String newStr = subtext(len, str);
			return append(len, textSize(newStr), newStr);
			// return append(len, getSize(str), str);
		} else {
			return append(len, size, str);
		}
	}

	// Joan 2014-08-20 WINCOR PCR
	protected String getPromoCoreQuery(boolean amend, String coy, String Store) {
		String core_query = null;
		if (amend == true) {
			core_query = "SELECT COY, PRC_CHG_ID, STATUS FROM ( "
					+ "SELECT   H.COY, H.PRC_CHG_NO_PREFIX||''||To_Char(H.PRC_CHG_NO) AS PRC_CHG_ID, H.PRC_CHG_STATUS AS STATUS "
					+ "FROM      PRCHGDET D, PRCHGHDR H, ITEMMST I, STRSEND S "
					+ "WHERE     H.COY=D.COY "
					+ "AND H.PRC_CHG_NO=D.PRC_CHG_NO "
					+ "AND To_Char(H.PRC_CHG_NO) = S.SS_RECORD_KEY "
					+ "AND S.SS_RECORD_TYPE='PRCHGHDR' "
					+ "AND H.COY = '" + coy + "' "
					+ "AND S.SS_STORE=D.STORE "
					+ "AND S.SS_STORE='" + Store + "' "
					+ "AND (SELECT LAST_DAY_UPDATED+1 FROM SYSCTL WHERE SYS_CTL_KEY='1') BETWEEN H.PRC_CHG_DATE_EFF AND H.PRC_CHG_DATE_END "
					+ "AND ((H.PRC_CHG_TRANS_TYPE IN (SELECT PROFITVV.VNM_VDTVL FROM PROFITVV WHERE PROFITVV.COY=H.COY "
					+ "AND PROFITVV.VNM IN ('TRNPromoTranstype', 'TRNMPromoPrchg'))) "
					+ "AND (H.PRC_CHG_STATUS = 'U' OR H.PRC_CHG_STATUS='R')) "
					+ "AND I.SHORT_SKU=D.SHORT_SKU "
					+ "AND I.NON_PLU='N' "
					+ "GROUP BY  H.COY, H.PRC_CHG_NO_PREFIX, H.PRC_CHG_NO, H.PRC_CHG_STATUS "
					+ ")UNION( "
					+ "SELECT   H.COY, H.PROMO_NO_PREFIX||''||H.PROMO_NO AS PRC_CHG_ID, H.MM_STATUS AS STATUS "
					+ "FROM     STRSEND S, MIXMHDR H, MIXMDET D, ITEMMST I "
					+ "WHERE    H.COY = D.COY "
					+ "AND H.PROMO_NO = D.PROMO_NO "
					+ "AND H.PROMO_NO = S.SS_RECORD_KEY "
					+ "AND S.SS_RECORD_TYPE='MIXMHDR' "
					+ "AND H.COY = '" + coy + "' "
					+ "AND S.SS_STORE = D.STORE "
					+ "AND D.STORE='" + Store + "' "
					+ "AND (H.MM_STATUS='R' OR H.MM_STATUS='C') "
					+ "AND I.SHORT_SKU=D.SHORT_SKU "
					+ "AND H.MM_START_DATE<=(SELECT (LAST_DAY_UPDATED+1) FROM SYSCTL WHERE SYS_CTL_KEY='1') "
					+ "GROUP BY H.COY, H.PROMO_NO_PREFIX, H.PROMO_NO, H.MM_STATUS ) ";
		} else {
			core_query = "SELECT COY, PRC_CHG_ID, STATUS FROM ( "
					+ "SELECT   H.COY, H.PRC_CHG_NO_PREFIX||''||To_Char(H.PRC_CHG_NO) AS PRC_CHG_ID, H.PRC_CHG_STATUS AS STATUS "
					+ "FROM  PRCHGDET D, PRCHGHDR H, ITEMMST I "
					+ "WHERE    H.COY=D.COY "
					+ "AND H.PRC_CHG_NO=D.PRC_CHG_NO "
					+ "AND H.COY = '" + coy + "' "
					+ "AND D.STORE='" + Store + "' "
					+ "AND (SELECT LAST_DAY_UPDATED+1 FROM SYSCTL WHERE SYS_CTL_KEY='1') BETWEEN H.PRC_CHG_DATE_EFF AND H.PRC_CHG_DATE_END "
					+ "AND ((H.PRC_CHG_TRANS_TYPE IN (SELECT PROFITVV.VNM_VDTVL FROM PROFITVV WHERE PROFITVV.COY=H.COY AND PROFITVV.VNM IN('TRNPromoTranstype','TRNMPromoPrchg'))) "
					+ "AND (H.PRC_CHG_STATUS = 'U' OR H.PRC_CHG_STATUS='R')) "
					+ "AND I.SHORT_SKU=D.SHORT_SKU "
					+ "AND I.NON_PLU='N' "
					+ "GROUP BY H.COY, H.PRC_CHG_NO_PREFIX, H.PRC_CHG_NO, H.PRC_CHG_STATUS "
					+ ") UNION ( "
					+ "SELECT   H.COY, H.PROMO_NO_PREFIX||''||H.PROMO_NO AS PRC_CHG_ID, H.MM_STATUS AS STATUS "
					+ "FROM     MIXMHDR H, MIXMDET D, ITEMMST I "
					+ "WHERE    H.COY = D.COY "
					+ "AND H.PROMO_NO = D.PROMO_NO "
					+ "AND H.COY = '" + coy + "' "
					+ "AND D.STORE='" + Store + "' "
					+ "AND (SELECT LAST_DAY_UPDATED+1 FROM SYSCTL WHERE SYS_CTL_KEY='1') BETWEEN H.MM_START_DATE AND H.MM_END_DATE "
					+ "AND (H.MM_STATUS='R' OR H.MM_STATUS='C') "
					+ "AND I.SHORT_SKU=D.SHORT_SKU "
					+ "GROUP BY H.COY, H.PROMO_NO_PREFIX, H.PROMO_NO, H.MM_STATUS ) ";
		}
		return core_query;

	}

	/*
	 * private int getSize(String str) { int size; if(charset==null ||
	 * charset.trim().length()==0) { charset = "GBK"; } try { size =
	 * str.getBytes(charset).length; }catch(Exception e) { size =
	 * str.getBytes().length; } return size; }
	 */

	private String append(int len, int size, String str) {
		int diff = len - size;
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < diff; i++) {
			buf.append(" ");
		}
		return str.concat(buf.toString());
	}

	private int textSize(String str) {
		int count = 0;
		char chars[] = str.toCharArray();

		for (int i = 0; i < chars.length; i++) {
			if (chars[i] > 127) {
				count = count + 2;
			} else {
				count = count + 1;
			}
		}
		return count;
	}

	private String subtext(int len, String str) {
		int count = 0;
		int offset = 0;

		for (int i = 0; i < str.length(); i++) {
			char iStr = str.charAt(i);
			if (iStr > 127) {
				count = count + 2;
			} else {
				count = count + 1;
			}

			if (count == len) {
				offset = i + 1;
				break;
			}
			if (count > len) {
				offset = i;
				break;
			}
		}
		return (str.substring(0, offset));
	}

	protected String getDate(java.sql.Date dt, String format) {
		if (dt == null)
			return blankDate(format);
		formatter.applyPattern(format);
		return formatter.format(dt);
	}

	private String blankDate(String format) {
		String ret = "";
		for (int i = 0; i < format.length(); i++) {
			ret = ret + " ";
		}
		return ret;
	}

	protected String getTime(String str) {
		if (str == null)
			return qrMisc.rightFillBlank("", 4);

		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			if (Character.isDigit(str.charAt(i))) {
				buf.append(str.charAt(i));
			}
		}

		if (buf.toString().trim().length() == 0) {
			str = qrMisc.rightFillBlank("", 4);
		} else if (buf.toString().trim().length() == 4) {
			str = buf.toString();
		} else {
			str = buf.toString().substring(0, 4);
		}

		return str;
	}

	protected String getIntValue(double val) {
		int ret = (int) val;
		return String.valueOf(ret);
	}

	protected String getValue(double val, int len, int places, boolean decimal) {
		return getValue(String.valueOf(val), len, places, decimal);
	}

	protected String getValue(String val, int len, int places, boolean decimal) {
		String str = qrMath.formatDecimal(val, places);

		if (decimal == false)
			str = str.replaceAll("[.]{1,}", ""); // replace all '.' with space

		return qrMisc.leftFillZero(str, len);
		// int diff = len - str.length();
		// for(int i=0; i<diff; i++)
		// {
		// str = "0" + str;
		// }
		// return str;
	}

	protected abstract String getQuery();

	protected abstract void processRS(ResultSet _rs) throws Exception;
}