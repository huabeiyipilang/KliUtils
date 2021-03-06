package cn.kli.utils.dao;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import cn.kli.utils.CollectionUtils;
import cn.kli.utils.klilog;

public class BaseDao<T extends BaseInfo> {
    private static final klilog LOG = new klilog(BaseDao.class);

    private Class<T> mClazz;
    private String mTableName;
    private List<Field> mDbFields = new ArrayList<Field>();
    private List<String> mColumnNames = new ArrayList<String>();
    private int mMaxRowCount;
    private BaseDatabaseHelper mOpenHelper;

    public BaseDao(Context context, Class<T> clazz, BaseDatabaseHelper dbHelper) {
        this(context, clazz, dbHelper, -1);
    }

    public BaseDao(Context context, Class<T> clazz, BaseDatabaseHelper dbHelper, int maxRowCount) {
        mClazz = clazz;
        mTableName = BaseInfo.getTableName(clazz);
        mMaxRowCount = maxRowCount;
        mOpenHelper = dbHelper;
        mOpenHelper.registerTable(clazz);

        retrieveFieldInfos();
    }

    public void beginbeginTransaction() {
        mOpenHelper.getWritableDatabase().beginTransaction();
    }

    public void setTransactionSuccessful() {
        mOpenHelper.getWritableDatabase().setTransactionSuccessful();
    }

    public void endTransaction() {
        mOpenHelper.getWritableDatabase().endTransaction();
    }

    public long insert(T data) {
        return insert(data, null);
    }

    public long insert(T data, SQLiteDatabase database) {
        long result = -1;
        SQLiteDatabase db;
        if (database == null) {
            db = mOpenHelper.getWritableDatabase();
        } else {
            db = database;
        }

        // 超过条数限制则复用最后一个记录
        if (mMaxRowCount > 0 && getRowCount(db) >= mMaxRowCount) {
            data.id = last().id;
            data.createdAt = getCurrentTimestamp();

            update(data);
        } else {
            try {
                ContentValues values = new ContentValues();

                data.createdAt = getCurrentTimestamp();
                data.updatedAt = data.createdAt;

                for (Field field : mDbFields) {
                    DbField dbFieldAnnotation = field.getAnnotation(DbField.class);
                    if (dbFieldAnnotation != null) {
                        if (!(field.getName().equals("id"))) {
                            setFieldValue(data, values, field, dbFieldAnnotation);
                        }
                    }
                }

                result = db.insert(mTableName, null, values);
            } catch (IllegalAccessException e) {
                LOG.e(e.toString());
            }
        }

        return result;
    }

    public T first() {
        T result = null;

        List<T> values = find(null, null, null, null, "`updated_at` DESC");
        if (!(values.isEmpty())) {
            result = values.get(0);
        }

        return result;
    }

    public T last() {
        T result = null;

        List<T> values = find(null, null, null, null, "`updated_at` ASC");
        if (!(values.isEmpty())) {
            result = values.get(0);
        }

        return result;
    }

    public int getRowCount() {
        return getRowCount(null);
    }

    public int getRowCount(SQLiteDatabase database) {
        SQLiteDatabase db = null;
        if (database == null) {
            db = mOpenHelper.getReadableDatabase();
        } else {
            db = database;
        }
        Cursor c = db.query(mTableName, null, null, null, null, null, null);
        int count = 0;
        if (c != null) {
            count = c.getCount();
            c.close();
        }
        return count;

    }

    public int delete(long id) {
        return deleteBy("_id", Long.toString(id));
    }

    public int deleteBy(String columnName, String value) {
        return mOpenHelper.getWritableDatabase()
                .delete(mTableName, escapeColumnName(columnName) + " = ?", new String[] { value });
    }

    public int delete(String whereClause, String[] whereArgs) {
        return mOpenHelper.getWritableDatabase()
                .delete(mTableName, whereClause, whereArgs);
    }
    public int update(T data) {
        int result = 0;

        BaseInfo info = (BaseInfo) data;

        try {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            ContentValues values = new ContentValues();

            data.updatedAt = getCurrentTimestamp();

            for (Field field : mDbFields) {
                DbField annotation = field.getAnnotation(DbField.class);
                if (annotation != null) {
                    setFieldValue(data, values, field, annotation);
                }
            }

            result = db.update(mTableName, values, "`_id` = ?", new String[] { Long.toString(info.id) });
        } catch (IllegalAccessException e) {
            LOG.e(e.toString());
        }

        return result;
    }

    public T find(int id) {
        T instance = null;

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = db.query(mTableName, null, "`_id` = ?", new String[] { Long.toString(id) }, null, null, null);
        if (c != null && c.moveToNext()) {
            instance = this.fillData(c);
        }
        if (c != null) {
            c.close();
        }

        return instance;
    }

    public List<T> findAll() {
        return find("", null, null, null, null);
    }

    public List<T> findAllOrderByUpdatedAt() {
        return find("", null, null, null, "`updated_at` DESC");
    }

    public T find(String selection, String[] selectArgs, String sortOrder) {
        List<T> dataList = find(selection, selectArgs, null, null, sortOrder);

        return dataList.isEmpty() ? null : dataList.get(0);
    }

    public T findBy(String columnName, String value) {
        List<T> dataList = find(escapeColumnName(columnName) + " = ?", new String[] { value }, null, null, null);

        return dataList.isEmpty() ? null : dataList.get(0);
    }

    public List<T> find(String selection, String[] selectArgs, String groupBy, String having, String sortOrder) {
        List<T> dataList = null;

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = db.query(mTableName, null, selection, selectArgs, groupBy, having, sortOrder);
        if (c != null) {
            dataList = fillList(c);
            c.close();
        }

        return dataList;
    }

    public Cursor query(String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return mOpenHelper.getReadableDatabase()
                .query(mTableName, null, selection, selectionArgs, groupBy, having, orderBy);
    }

    public boolean exist(String selection, String[] selectionArgs) {
        boolean result = false;

        Cursor cursor = mOpenHelper.getReadableDatabase()
                .query(mTableName, null, selection, selectionArgs, null, null, null);
        result = cursor.getCount() > 0;
        cursor.close();

        return result;
    }

    public void touch(T record) {
        update(record);
    }

    public String getTableName() {
        return mTableName;
    }

    private void retrieveFieldInfos() {
        Field[] fields = mClazz.getFields();
        for (Field field : fields) {
            DbField annotation = field.getAnnotation(DbField.class);
            if (annotation != null) {
                mDbFields.add(field);
                mColumnNames.add(getColumnName(field, annotation));
            }
        }
    }

    private void setFieldValue(T data, ContentValues values, Field field, DbField annotation)
            throws IllegalAccessException {
        String columnName = escapeColumnName(getColumnName(field, annotation));
        switch (annotation.type()) {
        case TEXT:
            values.put(columnName, String.valueOf(field.get(data)));
            break;

        case BLOB:
            break;

        case INTEGER:
            Object o = field.get(data);
            if (o instanceof Integer) {
                values.put(columnName, (Integer) o);
            } else {
                values.put(columnName, (Long) o);
            }
            break;

        case BIGINT:
            values.put(columnName, (Long) field.get(data));
            break;

        case REAL:
            values.put(columnName, (Float) field.get(data));
            break;

        case CALENDAR:
            values.put(columnName, ((Calendar)field.get(data)).getTimeInMillis());
            break;
            
        case INTARRAY:
            int[] intArray = (int[])field.get(data);
            values.put(columnName, intArray2String(intArray));
            
        default:
            break;
        }
    }
    
    private String intArray2String(int[] ints){
        if(ints == null){
            return null;
        }else{
            String res = "";
            for(int i = 0; i < ints.length; i++){
                res += ints[i];
                if(i != ints.length - 1){
                    res += ",";
                }
            }
            return res;
        }
    }

    private String getColumnName(Field field, DbField annoation) {
        String columnName = annoation.name();
        if (TextUtils.isEmpty(columnName)) {
            columnName = field.getName();
        }

        return columnName;
    }

    @SuppressWarnings("unchecked")
    public Class<T> getClassT() {
        Type type = getClass().getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) type;

        return (Class<T>) parameterizedType.getActualTypeArguments()[0];
    }

    public List<T> fillList(Cursor cursor) {
        List<T> dataList = new ArrayList<T>();
        while (cursor.moveToNext()) {
            T entry = fillData(cursor);
            if (entry != null) {
                dataList.add(entry);
            }
        }

        return dataList;
    }

    public Object getFieldValue(Cursor cursor, DbField annoation, String columnName) {
        Object result = null;
        final int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex != -1) {
            switch (annoation.type()) {
            case TEXT:
                result = cursor.getString(columnIndex);
                break;

            case BLOB:
                break;

            case INTEGER:
                result = cursor.getLong(columnIndex);
                break;

            case REAL:
                result = cursor.getFloat(columnIndex);
                break;

            case CALENDAR:
                Calendar calendar = new GregorianCalendar();
                calendar.setTimeInMillis(cursor.getLong(columnIndex));
                result = calendar;
                break;
                
            case INTARRAY:
                String arrayString = cursor.getString(columnIndex);
                String[] codes = arrayString.split(",");
                int[] intArray = CollectionUtils.stringArray2IntArray(codes);
                result = intArray;
                
            default:
                break;
            }
        }
        return result;
    }

    public T fillData(Cursor cursor) {
        T instance = null;
        try {
            instance = mClazz.newInstance();
        } catch (IllegalAccessException e) {
            LOG.e(e.toString());
        } catch (InstantiationException e) {
            LOG.e(e.toString());
        }
        for (Field field : mDbFields) {
            DbField annoation = field.getAnnotation(DbField.class);
            if (annoation != null) {
                String columnName = getColumnName(field, annoation);
                try {
                    Object value = getFieldValue(cursor, annoation, columnName);
                    if (value != null) {
                        if (field.getType().equals(int.class)) {
                            field.set(instance, ((Long) value).intValue());
                        } else {
                            field.set(instance, value);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    LOG.e(e.toString());
                } catch (IllegalAccessException e) {
                    LOG.e(e.toString());
                }
            }
        }

        return instance;
    }

    private String escapeColumnName(String columnName) {
        return "`" + columnName + "`";
    }

    private long getCurrentTimestamp() {
        return (new Date()).getTime();
    }
}
