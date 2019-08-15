import com.souche.perf.mock.MockResult;
import com.souche.perf.mock.MockService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;

public class MockServiceTest {


    @Test
    public void testMockServiceGetResult() {

        MockResult result = MockService.getInstance().getHttpMockResult("http://www.baidu.com",false,MockService.HTTP);

        System.out.println("result:" + result);
        Assert.assertNotNull(result);
    }

    @Test
    public void testMockServiceGetResultFromMockPlatform() {

        long startTime = System.currentTimeMillis();
        MockResult result = MockService.getInstance().getHttpMockResult("https://mock.souche-inc.com/mock/5d0a07e43c399765d8886112/example/upload",true,MockService.HTTP);

        System.out.println("result:" + result + " ,timeSpent:" + (System.currentTimeMillis() - startTime));
        Assert.assertNotNull(result);
    }


    @Test
    public void testMockServiceGetHttpResponse() {

        /*try {
            long startTime = System.currentTimeMillis();
            CloseableHttpResponse result = MockService.getInstance().getMockHttpCloseableResponse("hello world");

            System.out.println("result:" + EntityUtils.toString(result.getEntity()) + " ,timeSpent:" + (System.currentTimeMillis() - startTime));
            Assert.assertNotNull(EntityUtils.toString(result.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
}
