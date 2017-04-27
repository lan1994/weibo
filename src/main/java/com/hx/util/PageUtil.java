package com.hx.util;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xinxulan on 2017/4/26.
 */
public class PageUtil {
    public static Map<String, Object> execute(int nextPage, int pageSize, int rowCount, int pageDisplay) throws Exception {
        /**
         *
         * @param  nextPage:下一页页码
         * @param  pageSize:每页显示的记录条数
         * @param  rowCount:db中记录总数
         * @param  pageDisplay:显示多少个页码
         * @return offset,limit : select * from table where XX limit offset,limit;
         *         nextPage : 响应后应该显示的页码
         *         pageArray: page组件应该display的页码
         * @exception 完整类名 说明
         * @deprecated
         *
         * */
        Map<String, Object> results = new HashMap<>();
        if(nextPage <= 0){
            nextPage = 1;
        }
        if(pageSize <= 0){
            pageSize = 10;
        }
        if(rowCount < 0){
            throw new Exception(String.format("rowCount不能为负数[rowCount:%d]",rowCount));
        }
        if(rowCount == 0){
            results.put("status",0);
            return results;
        }
        int offset = 0;
        int limit = 0;
        int pageCount = rowCount / pageSize;
        int left = rowCount % pageSize;
        if (left > 0){
            pageCount += 1;
        }
        if (nextPage >= pageCount){
            offset = (pageCount - 1) * pageSize;
            limit = left==0?pageSize:left;
            nextPage = pageCount;
        }else {
            offset = (nextPage - 1) * pageSize;
            limit = pageSize;
        }
        int[] pageArray = null;
        if (pageDisplay >= pageCount)
        {
            pageArray = getPageDisplayArray(1,pageCount);
        }
        else
        {
            int minDividePage = pageDisplay/2;
            if (nextPage>minDividePage){
                int fromPage = nextPage - minDividePage;
                int len = pageDisplay;
                if (fromPage + pageDisplay >= pageCount){
                    len = pageCount - fromPage + 1;
                }
                pageArray = getPageDisplayArray(fromPage, len);
            }else {
                pageArray = getPageDisplayArray(1, pageDisplay);
            }
        }

        results.put("offset", offset);
        results.put("limit", limit);
        results.put("nextPage", nextPage);
        results.put("pageArray", pageArray);
        return results;
    }

    private static int[] getPageDisplayArray(int fromPage, int len){
        int[] pageArray = new int[len];
        for (int i = 0; i<len; i++){
            pageArray[i] = i+fromPage;
        }
        return pageArray;
    }

    public static void main(String[] args) throws Exception {

        Map<String,Object> res = PageUtil.execute(8,10,1000,12);
        System.out.println(res);
    }
}
