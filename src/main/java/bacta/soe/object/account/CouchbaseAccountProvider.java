/*
 * Created by IntelliJ IDEA.
 * User: Kyle
 * Date: 4/3/14
 * Time: 8:50 PM
 */
package bacta.soe.object.account;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ocdsoft.bacta.swg.network.soe.data.CouchbaseDatabaseConnector;
import com.ocdsoft.network.object.account.Account;

public class CouchbaseAccountProvider implements Provider<Account> {

    @Inject
    private CouchbaseDatabaseConnector connector;

    public Account get() {
        return new CouchbaseAccount(connector.nextAccountId());
    }
}
