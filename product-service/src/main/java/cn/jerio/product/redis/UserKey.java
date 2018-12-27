package cn.jerio.product.redis;

/**
 * Created by Jerio on 2018/3/13.
 */
public class UserKey extends BasePrefix {
    public UserKey(String prefix) {
        super(prefix);
    }
    public static UserKey getById = new UserKey("id");
    public static UserKey getByName = new UserKey("name");
}
