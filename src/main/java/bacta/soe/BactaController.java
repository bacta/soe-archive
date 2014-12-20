package bacta.soe;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by kburkhardt on 2/21/14.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface BactaController {
    
    public static int GAMESERVERUPDATE = 0x0;

    int command();

    ServerType server();
}
