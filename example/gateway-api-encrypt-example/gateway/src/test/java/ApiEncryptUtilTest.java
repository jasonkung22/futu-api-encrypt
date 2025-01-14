import cn.futuai.open.encrypt.core.util.ApiEncryptUtil;

/**
 * ApiEncryptUtilTest
 * @author Jason Kung
 * @date 2024/10/22 14:01
 */
public class ApiEncryptUtilTest {


    public static void main(String[] args) {
        System.out.println(ApiEncryptUtil.sign("1729576544119", "JErEG2TMqxTUHzChDrkuFSWXJjjv7Rbp", "",
                "cH0hG5eyH9SwSUQ0vfzBIOcbtYDRcHgNNqurLlLsvdOrt1GHtkD9nM6ekkiKdNw+"));
    }
}
