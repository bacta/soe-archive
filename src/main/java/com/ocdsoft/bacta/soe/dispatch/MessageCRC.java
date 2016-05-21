package com.ocdsoft.bacta.soe.dispatch;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by kyle on 5/19/2016.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageCRC {
    int value();
}
