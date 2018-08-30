// changes
// guoliang 2014-05-21 AU-IM002701122 check ITEMCTRLMST.BLOCK_POS_SELL when SYSPosCtrlItem is 'Y'
// guoliang 2014-06-3 Split Business Unit PCR
// JoeMy 2015-01-15 AMY GST Implementation (AU-SD011432058)

package qrcom.PROFIT.files.strsend.pos;
import java.sql.*;
import java.util.*;
import qrcom.PROFIT.files.info.*;
import qrcom.util.*;

public class PosItemmstDownload extends PosFileInfo
{
   private ItemPrcChgDownload itemPrcChgDownload = new ItemPrcChgDownload();

   public PosItemmstDownload(Connection cn, ArrayList _storeList)
   {
      this.conn = cn;
      this.STORELIST = _storeList;
   }
   
   private void initVar()
   {
      strmstInfo = ((StrmstInfo)STORELIST.get(index));
   }
      
   public void downloadWholeTable(int n) throws Exception
   {
      super.index = n;
      super.amended = false;
      
      this.initVar();
      super.execute();
   }
   
   public void downloadAmendedRecords(int n) throws Exception
   {
      super.index = n;
      super.amended = true;
      
      this.initVar();
      super.execute();
   }
   
   public void deleteAmendedRecords(int n, DeleteAmended deleteAmended) throws Exception
   {
      super.index = n;
      super.amended = true;
      this.initVar();
      String qry = getDeleteQuery();
      deleteAmended.delete(qry);
      ResultSet _rs1 = itemPrcChgDownload.getResultSet(conn, amended, strmstInfo.STORE());
      deleteAmended.delete(_rs1);
   }
   
   private int line = 0;
   
   protected void processRS(ResultSet rset) throws Exception
   {
      try
      {
         line = 0;
         String SEQ = qrMisc.leftFillZero(String.valueOf(seqSQL.getNextItemSeq(strmstInfo.STORE())), 2);
         WRITER.setPath(strmstInfo.STORE(), PosFileInfo.ITEMMST, SEQ);
      
         //for itemmst
         writeItemFile(rset);
         //for mku and mkd 
         writeItemFile(itemPrcChgDownload.getResultSet(conn, amended, strmstInfo.STORE()));
      }
      finally
      {
         try
         {
            if(psBarcode!=null)
               psBarcode.close();
         }catch(Exception e){}
         psBarcode = null;
         
         if(itemPrcChgDownload!=null)
            itemPrcChgDownload.closeStatement();
      }
   }
   
   private void writeItemFile(ResultSet resultSet) throws Exception {
        double dbl, t;
        String str, SKU;
        String mommyFlag, mbrDiscFlag;

        while (resultSet != null && resultSet.next()) {
            if (line > 0) {
                bufStr.append("\r\n");
            }
            line++;

            //REC_ID
            if (amended == true) {
                String act = resultSet.getString("SS_ACTION_TYPE");
                if (act != null && act.equals("D"))
                    bufStr.append("D");
                else {
                    act = resultSet.getString("NO_SELLING_FLAG");
                    if (act != null && act.equalsIgnoreCase("Y"))
                        bufStr.append("D");
                    else
                        bufStr.append("A");
                }
            } else
                bufStr.append("A");

            //ITEM_NO
            SKU = resultSet.getString("SHORT_SKU");
            bufStr.append(qrMisc.rightFillBlank(getShortSku(SKU), 13));

            //ITEM_SHORT_NAME
            String plu_desc = resultSet.getString("ITEM_PLU_DESC");
            if (plu_desc == null)
                plu_desc = "";
            bufStr.append(getDesc(PosFileInfo.LANGCODE, plu_desc, 15));

            //ITEM_LONG_NAME
            String item_desc = resultSet.getString("ITEM_DESC");
            if (item_desc == null)
                item_desc = "";
            bufStr.append(getDesc(PosFileInfo.LANGCODE, item_desc, 30));

            //ITEM_SHORT_NAME_CHINESE
            bufStr.append(getDesc(PosFileInfo.LANGCODE, plu_desc, 20));

            //ITEM_LONG_NAME_CHINESE
            bufStr.append(getDesc(PosFileInfo.LANGCODE, item_desc, 40));

            //ITEM_BARCODE
            bufStr.append(getBarcode(getPrimaryBarcode(SKU)));

            //ITEM_SELL
            dbl = resultSet.getDouble("SELL_PRC");
            if (dbl <= 0)
                bufStr.append(getValue(resultSet.getDouble("GST_SELL"), 8, 2, true));
            else
                bufStr.append(getValue(dbl, 8, 2, true));

            //ITEM_MEMBER_SELL
            t = resultSet.getDouble("GST_SELL_MEMBER");
            dbl = resultSet.getDouble("SELL_MEM_PRC");
            if (dbl <= 0)
                bufStr.append(getValue(t, 8, 2, true));
            else
                bufStr.append(getValue(dbl, 8, 2, true));

            //ITEM_UOM
            str = resultSet.getString("ITEM_SELL_UNIT");
            if (str == null)
                str = "";
            bufStr.append(qrMisc.rightFillBlank(str, 5));

            //ITEM_DIV
            str = resultSet.getString("DIV");
            if (str == null)
                str = "";
            bufStr.append(qrMisc.rightFillBlank(str, 3));

            //ITEM_DEPT
            str = resultSet.getString("DEPT");
            if (str == null)
                str = "";
            bufStr.append(qrMisc.rightFillBlank(str, 3));

            //ITEM_CLS
            str = resultSet.getString("CLASS");
            if (str == null)
                str = "";
            bufStr.append(qrMisc.rightFillBlank(str, 6));

            //ITEM_SUBCLS
            str = resultSet.getString("SUBCLASS");
            if (str == null)
                str = "";
            bufStr.append(qrMisc.rightFillBlank(str, 9));

            //ITEM_WEIGH
            str = resultSet.getString("ITEM_WEIGH_CD");
            if (str == null)
                str = "";
            bufStr.append(qrMisc.rightFillBlank(str, 1));

            //ITEM_PLU_FLAG
            //str = resultSet.getString("ITEM_PRICE_OVERRIDE");
            str = resultSet.getString("NON_PLU");
            if (str != null && str.equals("Y"))
                str = "1";
            else
                str = "2";

            bufStr.append(str);
            //bufStr.append("2");

            //ITEM_DATE
            bufStr.append(getDate(resultSet.getDate("DATE_CREATE"), "yyyyMMdd"));

            //ITEM_VAT_FLAG
            dbl = resultSet.getDouble("GST_SELL_RATE");
            if (dbl > 0)
                bufStr.append(qrMisc.rightFillBlank("1", 1));
            else
                bufStr.append(qrMisc.rightFillBlank("2", 1));

            //ITEM_VAT%
            //JoeMy 2015-01-30 GST PCR
            if (PosFileInfo.SYSDwnDefSellVat != null && PosFileInfo.SYSDwnDefSellVat.equals("Y")) {
                bufStr.append(qrMisc.leftFillZero(qrMath.noDecimal(PosFileInfo.SYSSellVATBefGST), 3));
            } else {
                bufStr.append(qrMisc.leftFillZero(qrMath.noDecimal(String.valueOf(dbl)), 3));
            }


            //ITEM_SEASON
            bufStr.append(qrMisc.rightFillBlank(resultSet.getString("ITEM_SEASON"), 6));

            //ITEM_TAX_RATE%
            dbl = resultSet.getDouble("ITEM_TAX_RATE");
            bufStr.append(qrMisc.leftFillZero(qrMath.noDecimal(String.valueOf(dbl)),
                                              3)); //CHUA 2010-02-23 - change to leftFillZero instead of rightFillZero

            //KADS1M_FLAG
            str =
                resultSet.getString("KADS1M_FLAG"); //Esther - 2013-02-25: SD003991190 added new field, KADS1M item flag
            if (str != null && str.equals("Y"))
                str = "Y";
            else
                str = "N";
            bufStr.append(str);

            //Valid Use Date
            //JoeMy 2015-01-15 AMY GST Implementation (AU-SD011432058)
            bufStr.append(qrMisc.leftFillZero(resultSet.getString("ITEM_SHELF"), 3));

            //Card Flag
            //JoeMy 2015-01-15 AMY GST Implementation (AU-SD011432058)
            mommyFlag = resultSet.getString("MOMMY_ITEM") == null ? "N" : resultSet.getString("MOMMY_ITEM");
            mbrDiscFlag = resultSet.getString("MBR_DISC_ITEM") == null ? "N" : resultSet.getString("MBR_DISC_ITEM");

            if (mommyFlag.equals("N") && mbrDiscFlag.equals("N")) {
                str = "0";
            } else if (mommyFlag.equals("Y") && mbrDiscFlag.equals("N")) {
                str = "1";
            } else if (mommyFlag.equals("N") && mbrDiscFlag.equals("Y")) {
                str = "2";
            } else if (mommyFlag.equals("Y") && mbrDiscFlag.equals("Y")) {
                str = "3";
            }

            bufStr.append(str);

            //VAT Code
            //JoeMy 2015-01-15 AMY GST Implementation (AU-SD011432058)
            dbl = resultSet.getDouble("ITEM_TAX_RATE");
            if (PosFileInfo.SYSDwnDefSellVat != null && PosFileInfo.SYSDwnDefSellVat.equals("Y")) {
                bufStr.append(qrMisc.rightFillBlank(qrMath.noDecimal(PosFileInfo.SYSSellVATBefGST), 5));
            } else {
                if (dbl > 0) {
                    bufStr.append(qrMisc.rightFillBlank(PosFileInfo.SYSSalesTaxCode, 5));
                } else {
                    bufStr.append(qrMisc.rightFillBlank(resultSet.getString("ITEM_SELL_VAT_CODE"), 5));
                }
            }

            //ADDITIONAL_DISC_ITEM
            // added by Alan Heng Poh Loon - 24-07-2017 SRAM067156 Additional Auto Discount System Enhancement (new column 'ADDITIONAL_DISC_ITEM')
            if (PosFileInfo.SYSAddAutoDiscFlg != null && PosFileInfo.SYSAddAutoDiscFlg.equals("Y")) {
                if (resultSet.getString("ADDITIONAL_DISC_ITEM") != null &&
                    resultSet.getString("ADDITIONAL_DISC_ITEM").equals("Y")) {
                    bufStr.append("Y");
                } else {
                    bufStr.append("N");
                }
            }

            writeToFile();

            bufStr = null;
            bufStr = new StringBuffer();
        } //while
    }
   
   private PreparedStatement psBarcode = null;
   
   
   private String getPrimaryBarcode(String sku) throws Exception
   {
      String barcode = "";
      ResultSet rs = null;

      String barQuery = "SELECT BARCODE, BARCD_PRIMARY " +
                                 "\nFROM XBARCODE " +
                                 "\nWHERE SHORT_SKU=? " +
                                 "\n  AND STORE_CATEGORY = '" + strmstInfo.STORE_CATEGORY() + "'"; // guoliang - Split Business Unit PCR
           
      try
      {
         String tmp = "";
         if(psBarcode==null)
            psBarcode = conn.prepareStatement(barQuery);
         psBarcode.setString(1, sku);
         rs = psBarcode.executeQuery();
         while(rs!=null && rs.next())
         {
            if(rs.getString("BARCD_PRIMARY").equals("Y"))
            {
               barcode = rs.getString("BARCODE");
               break;
            }
            tmp = rs.getString("BARCODE");
         }
         
         if(barcode.trim().length()==0)
            barcode = tmp;
      }
      finally
      {
         try
         {
            if(rs != null) 
               rs.close();
         }catch(Exception e){}
         rs = null;
      }
      return barcode;
   }
   
   private String getDeleteQuery()
   { 
      /* Amended by Kelvin 20120606 - Issue Request AU-IM00666851 - today's STRSEND data will be 
       * deleted if previous day EOD delayed to the subsequent day.
       * Changes made: Filter SS_TRANS_DATE with LAST_DAY_UPDATED */
      String deleteQuery =       
         "SELECT DISTINCT STR.SHORT_SKU, STR.ITEM_PLU_DESC, STR.ITEM_DESC, " +
         "\nSTR.ITEM_SELL_UNIT, STR.DATE_CREATE, STR.ITEM_WEIGH_CD, " + 
         "\nSTR.DIV, STR.DEPT, STR.CLASS, STR.SUBCLASS, " +
         "\nSTR.GST_SELL_RATE, STR.GST_SELL, STR.GST_SELL_MEMBER AS GST_SELL_MEMBER, " +
         "\nCASE WHEN STR.ITEM_DEL_CD='Y' OR S.NO_SELLING_FLAG='Y' OR STR.SS_ACTION_TYPE = 'D' THEN " + // guoliang - AU-IM002701122
         "\n  'D' " +
         "\nELSE " +
         "\n  'E' " +
         "\nEND SS_ACTION_TYPE, " +
         "\nSTR.ITEM_PRICE_OVERRIDE, STR.NON_PLU, S.SELL_PRC, " +
         "\nS.SELL_MEM_PRC, S.SELL_PRC_UNIT, S.NO_SELLING_FLAG, STR.SS_TRANS_DATE, STR.SS_TRANS_SEQ, STR.ITEM_SEASON, STR.ITEM_TAX_RATE, STR.KADS1M_FLAG, " + //Esther - 2013-02-25: SD003991190 added new field, KADS1M item flag
         // added by Alan Heng Poh Loon - 24-07-2017 SRAM067156 Additional Auto Discount System Enhancement (new column 'ADDITIONAL_DISC_ITEM')
         "\nSTR.ITEM_WEIGHT_TYPE, STR.ITEM_SHELF, STR.MOMMY_ITEM, STR.MBR_DISC_ITEM, STR.ADDITIONAL_DISC_ITEM, STR.ITEM_SELL_VAT_CODE "  + //JoeMy 2015-01-15 AMY GST Implementation (AU-SD011432058)
         "\n FROM ( " +
            "\nSELECT I.SHORT_SKU, I.ITEM_PLU_DESC, I.ITEM_DESC, " +
            "\nI.ITEM_SELL_UNIT, I.DATE_CREATE, I.ITEM_WEIGH_CD, " + 
            "\nI.DIV, I.DEPT, I.CLASS, I.SUBCLASS, I.GST_SELL_RATE, " +
            "\nI.GST_SELL, I.GST_SELL_MEMBER, I.ITEM_PRICE_OVERRIDE, " +
            "\nI.NON_PLU, I.ITEM_DEL_CD, S.SS_TRANS_DATE, S.SS_TRANS_SEQ, S.SS_ACTION_TYPE, I.ITEM_SEASON, I.ITEM_TAX_RATE, I.KADS1M_FLAG, " + //Esther - 2013-02-25: SD003991190 added new field, KADS1M item flag
            // added by Alan Heng Poh Loon - 24-07-2017 SRAM067156 Additional Auto Discount System Enhancement (new column 'ADDITIONAL_DISC_ITEM')
            "\nI.ITEM_WEIGHT_TYPE, I.ITEM_SHELF, I.MOMMY_ITEM, I.MBR_DISC_ITEM, I.ADDITIONAL_DISC_ITEM, I.ITEM_SELL_VAT_CODE  " + //JoeMy 2015-01-15 AMY GST Implementation (AU-SD011432058)
         
            "\nFROM STRSEND S, ITEMMST I " +
         
            "\nWHERE S.SS_RECORD_KEY=I.SHORT_SKU " +
            "\nAND (S.SS_STORE='"+strmstInfo.STORE()+"' OR S.SS_STORE='SSSS') " +
            
            "\nAND (S.SS_RECORD_TYPE='ITEMMST' OR S.SS_RECORD_TYPE='WEIGH_ITEM' OR " +
               "\nS.SS_RECORD_TYPE='ITEMCTRLMST' OR S.SS_RECORD_TYPE='CI_WEIGH_ITEM') " + // guoliang - AU-IM002701122
            "\nAND S.SS_TRANS_DATE <= (SELECT LAST_DAY_UPDATED FROM SYSCTL)" +
         "\n)STR, SELPRMST S WHERE STR.SHORT_SKU=S.SHORT_SKU AND S.STORE='"+strmstInfo.STORE()+"' " +
         "\nORDER BY SHORT_SKU, SS_ACTION_TYPE";
         
         // System.out.println("ITEMMST DELETE QUERY----------------------------------"); // remove this
         // System.out.println(deleteQuery); // remove this
         // System.out.println("ITEMMST DELETE QUERY----------------------------------"); // remove this
         /*
         "SELECT STR.SHORT_SKU, STR.ITEM_PLU_DESC, STR.ITEM_DESC, " + 
         "STR.ITEM_SELL_UNIT, STR.DATE_CREATE, STR.ITEM_WEIGH_CD, " +
         "STR.DIV, STR.DEPT, STR.CLASS, STR.SUBCLASS, " +  
         "STR.GST_SELL_RATE, STR.GST_SELL, STR.GST_SELL_MEMBER AS GST_SELL_MEMBER,  " +
         "STR.SS_TRANS_DATE, STR.SS_TRANS_SEQ, STR.SS_ACTION_TYPE, STR.ITEM_PRICE_OVERRIDE, " +
         "STR.NON_PLU, S.SELL_PRC, S.SELL_MEM_PRC, S.SELL_PRC_UNIT, STR.ITEM_SEASON FROM " +
         
         "(SELECT I.SHORT_SKU, I.ITEM_PLU_DESC, I.ITEM_DESC, " +
         "I.ITEM_SELL_UNIT, I.DATE_CREATE, I.ITEM_WEIGH_CD, " +
         "I.DIV, I.DEPT, I.CLASS, I.SUBCLASS, " +    
         "I.GST_SELL_RATE, I.GST_SELL, I.GST_SELL_MEMBER, I.ITEM_PRICE_OVERRIDE, " + 
         "I.NON_PLU, S.SS_TRANS_DATE, S.SS_TRANS_SEQ, S.SS_ACTION_TYPE, I.ITEM_SEASON " +
         
         "FROM STRSEND S, ITEMMST I " +
         
         "WHERE S.SS_RECORD_KEY=I.SHORT_SKU AND " +
         "(S.SS_RECORD_TYPE='ITEMMST' OR S.SS_RECORD_TYPE='WEIGH_ITEM')) STR, " +
                  
         "SELPRMST S WHERE STR.SHORT_SKU=S.SHORT_SKU AND S.STORE='" + strmstInfo.STORE() + "'"
         ;*/
      return deleteQuery;
   }
   
   protected String getQuery()
   {
      if(amended==true)
      {  
         query =
         "SELECT DISTINCT STR.SHORT_SKU, STR.ITEM_PLU_DESC, STR.ITEM_DESC, " +
         "STR.ITEM_SELL_UNIT, STR.DATE_CREATE, STR.ITEM_WEIGH_CD, " + 
         "STR.DIV, STR.DEPT, STR.CLASS, STR.SUBCLASS, " +
         "STR.GST_SELL_RATE, STR.GST_SELL, STR.GST_SELL_MEMBER AS GST_SELL_MEMBER, " +
         "CASE WHEN STR.ITEM_DEL_CD='Y' OR S.NO_SELLING_FLAG='Y' OR STR.SS_ACTION_TYPE = 'D' THEN " + // guoliang - AU-IM002701122
         "  'D' " +
         "ELSE " +
         "  'E' " +
         "END SS_ACTION_TYPE, " +
         "STR.ITEM_PRICE_OVERRIDE, STR.NON_PLU, S.SELL_PRC, " +
         "S.SELL_MEM_PRC, S.SELL_PRC_UNIT, S.NO_SELLING_FLAG, STR.ITEM_SEASON, STR.ITEM_TAX_RATE, STR.KADS1M_FLAG, " + //Esther - 2013-02-25: SD003991190 added new field, KADS1M item flag
         // added by Alan Heng Poh Loon - 24-07-2017 SRAM067156 Additional Auto Discount System Enhancement (new column 'ADDITIONAL_DISC_ITEM')
         "STR.ITEM_WEIGHT_TYPE, STR.ITEM_SHELF, STR.MOMMY_ITEM, STR.MBR_DISC_ITEM, STR.ADDITIONAL_DISC_ITEM, STR.ITEM_SELL_VAT_CODE "  + //JoeMy 2015-01-15 AMY GST Implementation (AU-SD011432058)
         " FROM " + 
         "( " +
            "SELECT I.SHORT_SKU, I.ITEM_PLU_DESC, I.ITEM_DESC, " +
            "I.ITEM_SELL_UNIT, I.DATE_CREATE, I.ITEM_WEIGH_CD, " + 
            "I.DIV, I.DEPT, I.CLASS, I.SUBCLASS, I.GST_SELL_RATE, " +
            "I.GST_SELL, I.GST_SELL_MEMBER, I.ITEM_PRICE_OVERRIDE, " +
            "I.NON_PLU, I.ITEM_DEL_CD, S.SS_ACTION_TYPE, I.ITEM_SEASON, I.ITEM_TAX_RATE, I.KADS1M_FLAG, " + //Esther - 2013-02-25: SD003991190 added new field, KADS1M item flag
            // added by Alan Heng Poh Loon - 24-07-2017 SRAM067156 Additional Auto Discount System Enhancement (new column 'ADDITIONAL_DISC_ITEM')
            "I.ITEM_WEIGHT_TYPE, I.ITEM_SHELF, I.MOMMY_ITEM, I.MBR_DISC_ITEM, I.ADDITIONAL_DISC_ITEM, I.ITEM_SELL_VAT_CODE " + //JoeMy 2015-01-15 AMY GST Implementation (AU-SD011432058)
            "FROM STRSEND S, ITEMMST I " +
         
            "WHERE S.SS_RECORD_KEY=I.SHORT_SKU " +
            "AND (S.SS_STORE='"+strmstInfo.STORE()+"' OR S.SS_STORE='SSSS') " +
            "AND (S.SS_RECORD_TYPE='ITEMMST' OR S.SS_RECORD_TYPE='WEIGH_ITEM' " +
            "OR S.SS_RECORD_TYPE='ITEMCTRLMST' OR S.SS_RECORD_TYPE='CI_WEIGH_ITEM')" + // guoliang - AU-IM002701122
         ")STR, SELPRMST S WHERE STR.SHORT_SKU=S.SHORT_SKU AND S.STORE='"+strmstInfo.STORE()+"' " +
         "ORDER BY SHORT_SKU, SS_ACTION_TYPE";
         /*
         "SELECT STR.SHORT_SKU, STR.ITEM_PLU_DESC, STR.ITEM_DESC, " + 
         "STR.ITEM_SELL_UNIT, STR.DATE_CREATE, STR.ITEM_WEIGH_CD, " +
         "STR.DIV, STR.DEPT, STR.CLASS, STR.SUBCLASS, " +  
         "STR.GST_SELL_RATE, STR.GST_SELL, STR.GST_SELL_MEMBER AS GST_SELL_MEMBER, " +
         "STR.SS_ACTION_TYPE, STR.ITEM_PRICE_OVERRIDE, " +
         "STR.NON_PLU, S.SELL_PRC, S.SELL_MEM_PRC, S.SELL_PRC_UNIT, S.NO_SELLING_FLAG, STR.ITEM_SEASON FROM " +
         
         "(SELECT I.SHORT_SKU, I.ITEM_PLU_DESC, I.ITEM_DESC, " +
         "I.ITEM_SELL_UNIT, I.DATE_CREATE, I.ITEM_WEIGH_CD, " +
         "I.DIV, I.DEPT, I.CLASS, I.SUBCLASS, I.GST_SELL_RATE, " +
         "I.GST_SELL, I.GST_SELL_MEMBER, I.ITEM_PRICE_OVERRIDE, " + 
         "I.NON_PLU, S.SS_ACTION_TYPE, I.ITEM_SEASON " +
         
         "FROM STRSEND S, ITEMMST I " +
         
         "WHERE S.SS_RECORD_KEY=I.SHORT_SKU " +
         "AND (S.SS_STORE='" + strmstInfo.STORE() + "' OR S.SS_STORE='SSSS') " +
         "AND (S.SS_RECORD_TYPE='ITEMMST' OR S.SS_RECORD_TYPE='WEIGH_ITEM')) STR, " +
                  
         "SELPRMST S WHERE STR.SHORT_SKU=S.SHORT_SKU AND S.STORE='" + strmstInfo.STORE() + "' " +
                  
         "GROUP BY (STR.SHORT_SKU, STR.ITEM_PLU_DESC, STR.ITEM_DESC, " +  
         "STR.ITEM_SELL_UNIT, STR.DATE_CREATE, STR.ITEM_WEIGH_CD, " + 
         "STR.DIV, STR.DEPT, STR.CLASS, STR.SUBCLASS, " +   
         "STR.GST_SELL_RATE, STR.GST_SELL, STR.GST_SELL_MEMBER, " +  
         "STR.SS_ACTION_TYPE, STR.ITEM_PRICE_OVERRIDE, " + 
         "STR.NON_PLU, S.SELL_PRC, S.SELL_MEM_PRC, S.SELL_PRC_UNIT, S.NO_SELLING_FLAG, STR.ITEM_SEASON) " +
         
         "ORDER BY STR.SHORT_SKU, STR.SS_ACTION_TYPE";
         */
      }
      else
      {
         query = 
         "SELECT " + 
         "ITEMS.SHORT_SKU, ITEMS.ITEM_PLU_DESC, ITEMS.ITEM_DESC, " +
         "ITEMS.ITEM_SELL_UNIT, ITEMS.DATE_CREATE, ITEMS.ITEM_WEIGH_CD, " +
         "ITEMS.DIV, ITEMS.DEPT, ITEMS.CLASS, ITEMS.SUBCLASS, " +  
         "ITEMS.GST_SELL_RATE, ITEMS.GST_SELL, ITEMS.GST_SELL_MEMBER AS GST_SELL_MEMBER, ITEMS.ITEM_PRICE_OVERRIDE, " +  
         "ITEMS.NON_PLU, S.SELL_PRC, S.SELL_MEM_PRC, S.SELL_PRC_UNIT, S.NO_SELLING_FLAG, ITEMS.ITEM_SEASON, ITEMS.ITEM_TAX_RATE, ITEMS.KADS1M_FLAG,  " + //Esther - 2013-02-25: SD003991190 added new field, KADS1M item flag
         // added by Alan Heng Poh Loon - 24-07-2017 SRAM067156 Additional Auto Discount System Enhancement (new column 'ADDITIONAL_DISC_ITEM')
         "ITEMS.ITEM_WEIGHT_TYPE, ITEMS.ITEM_SHELF, ITEMS.MOMMY_ITEM, ITEMS.MBR_DISC_ITEM, ITEMS.ADDITIONAL_DISC_ITEM, ITEMS.ITEM_SELL_VAT_CODE  " +  //JoeMy 2015-01-15 AMY GST Implementation (AU-SD011432058)
         "FROM (SELECT DISTINCT I.SHORT_SKU, I.ITEM_PLU_DESC, I.ITEM_DESC, " +
         "I.ITEM_SELL_UNIT, I.DATE_CREATE, I.ITEM_WEIGH_CD, " +
         "I.DIV, I.DEPT, I.CLASS, I.SUBCLASS, " +    
         "I.GST_SELL_RATE, I.GST_SELL, I.GST_SELL_MEMBER, I.ITEM_PRICE_OVERRIDE, " +
         "I.NON_PLU, I.ITEM_SEASON, I.ITEM_TAX_RATE, I.KADS1M_FLAG, " + //Esther - 2013-02-25: SD003991190 added new field, KADS1M item flag
         // added by Alan Heng Poh Loon - 24-07-2017 SRAM067156 Additional Auto Discount System Enhancement (new column 'ADDITIONAL_DISC_ITEM')
         "I.ITEM_WEIGHT_TYPE, I.ITEM_SHELF, I.MOMMY_ITEM, I.MBR_DISC_ITEM, I.ADDITIONAL_DISC_ITEM, I.ITEM_SELL_VAT_CODE "  + //JoeMy 2015-01-15 AMY GST Implementation (AU-SD011432058)
         "FROM ITEMMST I WHERE I.ITEM_DEL_CD='N' AND I.ITEM_ACTIVE='Y') ITEMS" + 

         ", SELPRMST S WHERE S.SHORT_SKU=ITEMS.SHORT_SKU AND (S.NO_SELLING_FLAG='N' OR S.NO_SELLING_FLAG IS NULL) " +
         "AND S.STORE='" + strmstInfo.STORE() + "' ORDER BY ITEMS.SHORT_SKU";

         // as of spec revision 2.7 block for whole download (amended==false) only
         String query_ctrlmst = "SELECT SHORT_SKU, SUPPL, SUPPL_CONTRACT, BLOCK_POS_SELL, CI_DEL_CD" + // guoliang - AU-IM002701122
                                 "\nFROM ITEMCTRLMST " +
                                 "\nWHERE COY = '" + PosFileInfo.COY + "'" +
                                 "\nAND COY_SUB = '" + PosFileInfo.COY_SUB + "'" +
                                 "\nAND STORE = '" + strmstInfo.STORE() + "'" +
                                 "\nAND CI_DEL_CD = 'N'" ;

         if (PosFileInfo.SYSPosCtrlItem.equals("Y")) // guoliang - AU-IM002701122
         {
            query = "SELECT * FROM  (" + query + ")A left join (" + query_ctrlmst + ")C on A.SHORT_SKU = C.SHORT_SKU " +
                     "\nWHERE (C.BLOCK_POS_SELL IS NULL OR C.BLOCK_POS_SELL = 'N') " +
                     "\n  AND (C.CI_DEL_CD IS NULL OR C.CI_DEL_CD = 'N') " +
                     "\nORDER BY A.SHORT_SKU ";
         }
         // else we leave query unchanged
      }


      // System.out.println("Item Query--------------------------");
      // System.out.println(query);
      // System.out.println("Item Query--------------------------");
      
      return query;
   }
}