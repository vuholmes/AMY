package qrcom.PROFIT.files.dao.local.DI;

import java.sql.*;

import java.util.*;

import qrcom.PROFIT.files.info.AltDescUtil;
import qrcom.PROFIT.files.dao.IF.shared.QueryBasedPagingDAOHandller;
import qrcom.PROFIT.files.info.AuditTrailLogger;
import qrcom.PROFIT.files.strsend.StrsendCreator;
import qrcom.PROFIT.shared.Utility.ExecuteQuery;

import qrcom.util.ejb.connection.*;
import qrcom.util.*;

import qrcom.PROFIT.system.*;

public class DwnSlsByRcptRptDAO implements QueryBasedPagingDAOHandller {
    private Connection conn = null;
    private Connection connX = null;
    private PreparedStatement pstmt = null;
    private ResultSet resultSet = null;

    public DwnSlsByRcptRptDAO() {
    }

    public Collection checkInput(Vector vctDeptmst) throws Exception {
        Collection errorSets = new Vector();
        return (errorSets);
    }

    /* Method to list from the Search ACtion */
    public HParam selectQueryBasedWebViewList(HParam hParam) throws Exception {
        Collection rsCollection = new Vector();
        long total_rows_selected = 0;

        String query = "";
        String selection = "\n SELECT RPT_DATE, RPT_TIME_STAMP, TRANS_NO, STORE, RPT_BY, RPT_PATH ";
        String from = "\n FROM EODREPORT";
        String criteria = " ";
        String orderBy = "\n ORDER BY  RPT_DATE DESC, RPT_TIME_STAMP DESC";

        String BEGIN_ROW_NUM = hParam.getString("BEGIN_ROW_NUM");
        String END_ROW_NUM = hParam.getString("END_ROW_NUM");

        String RPT_CAT = (String) hParam.get("rptCategory");
        String COY = (String) hParam.get("coy");
        String COY_SUB = (String) hParam.get("coySub");
        String STORE = (String) hParam.get("store");
        String FROM_DATE = (String) hParam.get("frGenDate");
        String TO_DATE = (String) hParam.get("toGenDate");
        String USER_ID = (String) hParam.get("userID");

        criteria += "\n WHERE RPT_CATEGORY = '" + RPT_CAT + "'";
        criteria += "\n AND COY = '" + COY +"'";
        criteria += "\n AND COY_SUB = '" + COY_SUB +"'";
        
        if(STORE != null && STORE.length() > 0){
            criteria += "\n AND STORE = '" + STORE + "'";
        }
        
        if((FROM_DATE != null && FROM_DATE.length() > 0 ) &&
            (TO_DATE != null && TO_DATE.length() > 0 ) ){
            criteria += "\n AND RPT_DATE BETWEEN TO_DATE('" + FROM_DATE + "','yyyy-mm-dd') AND TO_DATE('" + TO_DATE + "','yyyy-mm-dd')";
        }
        if(USER_ID != null && USER_ID.trim().length() > 0) {
            criteria += "\n AND RPT_BY = '" + USER_ID + "'";
        }

        try {
            conn = DataSource.getLocalConnection();

            ExecuteQuery execQuery = new ExecuteQuery(conn);
            //Retrieve total number of records selected
            if (criteria.length() >= 0)
                query = "select count(rownum) from (" + selection + from + criteria + orderBy + ")";
            else
                query = "select count(rownum) from (" + selection + from + orderBy + ")";

            System.out.println("query :" + query);
            String[] strQuery = execQuery.selectSingleRowQuery(query.toString(), 1);
            total_rows_selected = Long.parseLong(strQuery[0]);

            if (criteria.length() > 0)
                query = selection + from + criteria + orderBy;
            else
                query = selection + from + orderBy;

            String sql_query =
                "select * from (select V0.*, rownum CN from (" + query.toString() + ") V0) V1 where V1.CN between " +
                BEGIN_ROW_NUM + " and " + END_ROW_NUM;

            pstmt = conn.prepareStatement(sql_query);
            resultSet = pstmt.executeQuery();
            while (resultSet != null && resultSet.next()) {
                String[] str = new String[6];
                str[0] = resultSet.getString("RPT_DATE");
                str[1] = resultSet.getString("RPT_TIME_STAMP");
                str[2] = resultSet.getString("TRANS_NO");
                str[3] = resultSet.getString("STORE");
                str[4] = resultSet.getString("RPT_BY");
                str[5] = resultSet.getString("RPT_PATH");
                rsCollection.add(str);
            }
        } // try
        catch (SQLException e) {
            throw (e);
        } finally {
            try {
                if (resultSet != null)
                    resultSet.close();
            } catch (Exception Ex) {
            }

            try {
                if (pstmt != null)
                    pstmt.close();
            } catch (Exception Ex) {
            }

            try {
                if (conn != null)
                    conn.close();
            } catch (Exception Ex) {
            }

        }
        HParam rParam = new HParam(); // object for return keys(TOTAL_ROWS, COLLECTION)
        rParam.put("COLLECTION", rsCollection);
        rParam.put("TOTAL_ROWS", String.valueOf(total_rows_selected));
        return (rParam);
    } // end of selectWebViewList()

}
