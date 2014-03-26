package cn.kli.utils.dao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import cn.kli.utils.klilog;
import cn.kli.utils.dao.DbField.DataType;

public class BaseDatabaseHelper extends SQLiteOpenHelper {
    private static final klilog LOG = new klilog(BaseDatabaseHelper.class);

    private static final String DATABASE_NAME = "app.db";
    private static final int DATABASE_VERSION = 1;

    private static List<Class<? extends BaseInfo>> mTables = new ArrayList<Class<? extends BaseInfo>>();

    private static BaseDatabaseHelper sInstance = null;

    public static void fini() {
        sInstance.close();
        sInstance = null;
    }

    public static BaseDatabaseHelper getInstance(Context context) {
        if(sInstance == null){
            sInstance = new BaseDatabaseHelper(context);
        }
        return sInstance;
    }

    public void close() {
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }



    private static void registerDatabaseTable(Class<? extends BaseInfo> tableClass) {

        if (!(mTables.contains(tableClass))) {
            mTables.add(tableClass);
        }
    }
    
    void registerTable(Class<? extends BaseInfo> tableClass){
        registerDatabaseTable(tableClass);
    }

    private BaseDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private void dropTable(SQLiteDatabase db, Class<? extends BaseInfo> tableClass) {
        dropTable(db, BaseInfo.getTableName(tableClass));
    }

    private void dropTable(SQLiteDatabase db, String tableName) {
        db.execSQL("DROP TABLE IF EXISTS " + tableName);
    }

    private void createTable(SQLiteDatabase db, Class<? extends BaseInfo> tableClass) {
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("CREATE TABLE IF NOT EXISTS " + BaseInfo.getTableName(tableClass) + " ( ");
        Field[] fields = tableClass.getFields();

        if (fields != null && fields.length > 0) {
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if (field != null) {
                    DbField annoation = field.getAnnotation(DbField.class);
                    if (annoation != null) {
                        String fieldName = annoation.name();
                        DataType tableType = annoation.type();
                        if (TextUtils.isEmpty(fieldName)) {
                            fieldName = field.getName();
                        }
                        sqlBuffer.append("`" + fieldName + "` " + tableType.toString() + " ");
                        if (!annoation.isNull()) {
                            sqlBuffer.append("NOT NULL ");
                        }
                        if (annoation.isPrimaryKey()) {
                            sqlBuffer.append("PRIMARY KEY ");
                        }
                        if (annoation.isAutoIncrement()) {
                            sqlBuffer.append("autoincrement");
                        }
                        sqlBuffer.append(",");
                    }
                }
            }
        }
        // 删除最后一个逗号,
        sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);

        sqlBuffer.append(" )");
        String sql = sqlBuffer.toString();
        db.execSQL(sql);
    }

    private void createTables(SQLiteDatabase db) {
        for (Class<? extends BaseInfo> tableClass : mTables) {
            createTable(db, tableClass);
        }
    }
    
}
