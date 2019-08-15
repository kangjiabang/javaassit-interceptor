import com.souche.perf.db.interceptor.ShadowDataSourceService;
import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;

public class ShadowDataSourceServiceTest {


    /**
     * 启动参数指定 -Dapp.id=fin-repayment
     */
    @Test
    public void getTrimedUrl() {
        String url = "  jdbc:mysql://mysql1.dev.scsite.net:3306/fin_repayment?allowMultiQueries=true";
        String result = ShadowDataSourceService.getInstance().getTrimedUrl(url);
        System.out.println("result:" + result);
        Assert.assertEquals(result, "jdbc:mysql://mysql1.dev.scsite.net:3306/fin_repayment");

    }

    /**
     * 启动参数指定 -Dapp.id=fin-repayment
     */
    @Test
    public void getShadowDataSourceByUrl() {
        String url = "  jdbc:mysql://mysql1.dev.scsite.net:3306/fin_repayment?allowMultiQueries=true";
        ShadowDataSourceService.DataSourceConfig DataSourceConfig = ShadowDataSourceService.getInstance().getShadowDataSourceByUrl(url);
        System.out.println("result:" + DataSourceConfig);
        Assert.assertTrue(DataSourceConfig != null);

    }


    @Test
    public void testPattern() {
        String url = "jdbc:mysql://mysql1.dev.scsite.net:3306/test";
        ShadowDataSourceService shadowDataSourceService = ShadowDataSourceService.getInstance();

        ShadowDataSourceService.DataSourceConfig dataSourceConfig = new ShadowDataSourceService.DataSourceConfig(url, "name", "password");
        Matcher matcher = shadowDataSourceService.pattern.matcher(url);

        if (matcher.matches()) {
            System.out.println(matcher.group(0));
            System.out.println(matcher.group(1));
            System.out.println(matcher.group(2));
            System.out.println(matcher.group(3));
        }
    }

    @Test
    public void testGetHostPortDataBase() {
        String url = "jdbc:mysql://mysql1.dev.scsite.net:3306/test";

        ShadowDataSourceService.DataSourceConfig dataSourceConfig = new ShadowDataSourceService.DataSourceConfig(url, "name", "password");

        System.out.println(dataSourceConfig.getHost());
        System.out.println(dataSourceConfig.getPort());
        System.out.println(dataSourceConfig.getDatabase());

    }
}
