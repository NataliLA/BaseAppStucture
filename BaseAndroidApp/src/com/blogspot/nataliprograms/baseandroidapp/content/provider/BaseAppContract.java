package com.blogspot.nataliprograms.baseandroidapp.content.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class BaseAppContract {

    public static final String CONTENT_AUTHORITY = "com.blogspot.nataliprograms.baseandroidapp";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"
            + CONTENT_AUTHORITY);

    public static interface Tables {
        final String TEST_TABLE = "test_table";
    }

    public interface TestTableColumns extends BaseColumns {
        final String TEST_ID = "test_id";
        final String TEST_TEST = "test_test";

    }

    public static final class TestTable implements TestTableColumns {
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.baseandroidapp.testtable";

        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.baseandroidapp.tetstable";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(Tables.TEST_TABLE).build();

//        public static Uri buildContactsWithStatusesUri() {
//            return CONTENT_URI.buildUpon().appendPath(TEST_PATH).build();
//        }

    }

}
