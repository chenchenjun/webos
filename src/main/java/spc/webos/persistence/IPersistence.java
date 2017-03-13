
package spc.webos.persistence;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.dao.DataAccessException;

public interface IPersistence
{
	void addSqlMap(Map sqls);

	boolean contain(String sqlId);

	int update(Object po);

	/**
	 * ��ָ������������ΪWhere����
	 * 
	 * @param value
	 * @param whereProperties
	 *            �ö�����ָ����������Ϊupdate����where���������ΪNULLĬ�ϱ�ʾ������Ϊwhere����
	 * @param updateNULL
	 *            true��ʾ����Ѷ����е�NULL���Ը��µ����ݿ��еĶ�Ӧ���ֶ�
	 * 
	 */
	int update(Object po, String[] whereProps, boolean updateNULL);

	int update(Object po, List whereProps, boolean updateNULL);

	int update(Object po, String[] whereProps, String updateTail, boolean updateNULL, Map params);

	int update(Object po, List whereProps, String updateTail, boolean updateNULL, Map params);

	int insert(Object po);

	int insert(Object po, Map params);

	int delete(Object po, String[] whereProps, Map params);

	int delete(Object po, String[] whereProps, String deleteTail, Map params);

	int delete(Object po);

	// <T> List<T> getInSlave(T po);

	<T> List<T> get(T po);

	<T> List<T> get(T po, boolean lazyLoading);

	<T> List<T> get(T po, String[] assignedProps);

	<T> List<T> get(T po, Map params);

	/**
	 * 
	 * @param value
	 *            ��Ҫ�����ݿ���صĶ������ͣ��Լ���������
	 * @param assignedProperties
	 *            ָ����ȡ���ֶΣ����ԣ������������Ч��
	 * @param lazyLoading
	 *            ���Ϊfalse���ʾ��������˶�����صı�������ԣ�����ȡ��Ч�ʸ�
	 * @param paramMap
	 *            ��������ѯ������ΪNULL
	 * @return
	 */
	<T> List<T> get(T po, String[] assignedProps, boolean lazyLoading, boolean forUpdate,
			Map params);

	<T> List<T> get(T po, List assignedProps, boolean lazyLoading, boolean forUpdate, Map params);

	<T> T find(T po);

	<T> T find(T po, boolean lazyLoading);

	<T> T find(T po, Map params);

	<T> T find(T po, String[] assignedProps);

	<T> T find(T po, String[] assignedProps, boolean lazyLoading, boolean forUpdate, Map params);

	<T> T find(T po, List assignedProps, boolean lazyLoading, boolean forUpdate, Map params);

	Object query(String sqlID, Map params) throws DataAccessException;

	Map query(String[] sqlIds, Map params, Map results);

	Map query(List sqlIds, Map params, Map results);

	/**
	 * ͨ��SQlId, �Ͳ���ִ�в�ѯ���, ������
	 * 
	 * @param sqlID
	 * @param paramMap
	 * @return
	 */
	Object execute(String sqlId, Map params) throws DataAccessException;

	/**
	 * �ʺ��ڱ������, ��һ��SQLִ��, ���ѽ������SQL Id�ŵ�model��ȥ
	 * 
	 * @param sqlIDs
	 *            ��Ҫִ�е�һ��SQL
	 * @param paramMap
	 */
	Map execute(List sqlIds, Map params, Map results);

	Map execute(String[] sqlIds, Map params, Map results);

	/**
	 * ��õ�ǰ�־ò����õ�sql�ڵ�������Ϣ
	 * 
	 * @param sqlId
	 * @return
	 */
	Object getSQLConfig(String sqlId);

	/**
	 * ��ѯ�ã�����ǰ̨���ֱ�Ӵ���
	 */
	List query(Map params);

	List<Integer> update(List po);

	List<Integer> insert(List po);

	// batch operation
	int[] batchInsert(List po);

	int[] batch(String... sql);

	int[] batch(String sql, Object[] po);

	String insertSQL(Object po);

	String updateSQL(Object po);

	String updateSQL(Object po, String[] whereProps, String updateTail, boolean updateNULL,
			Map params);
	// batch operation end

	List<Integer> delete(List po);

	/**
	 * ��֤po
	 * 
	 * @param po
	 * @return
	 */
	<T> boolean validate(T po, int operator, Map paramMap);

	// �Ƿ���Ȩsql
	boolean isAuth(String sqlId, SQLItem item, Map params);

	// �Ƿ�sqlע�����
	boolean injection(String sqlId, SQLItem item, Map params);

	// 939_20170302 ��ǰʹ�õ�jdbctemplate
	public static ThreadLocal<String> CURRENT_JT = new ThreadLocal<>();

	public static Pattern SQL_INJECTION = Pattern.compile(
			"(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|"
					+ "(\\b(select|update|and|or|delete|insert|trancate|char|into|substr|ascii|declare|exec|count|master|into|drop|execute)\\b)",
			Pattern.CASE_INSENSITIVE);

	static String JAR_SQL_PATH = "classpath*:META-INF/persistence/sql/**/*.xml";
	static String JAR_VO_PATH = "classpath*:META-INF/persistence/mapping/**/*.xml";

	public static final String SELECT_ONLY = "SEL_ONLY"; // ֻ��ִ��select���
	public static final String GEN_SQL = "_GEN_SQL_"; // ֻ����sql,��ִ��
	public static final String QUERY_SQL_ID_KEY = "SQL_ID";
	public static final String PARAMS_KEY = "params";
	public static final String MATRIX_PREFIX = "M_";
	public static final String RESULT_CLASS_PREFIX = "_RC_";
	public static final String CLM_PREFIX = "_CLM_";
	public static final String SQL_PREFIX = "SQL_";
	public static final String ROW_INDEX_POSTFIX = "_RI";
	public static final String LAST_SQL_KEY = "_SQL_";
	public static final String UUID_KEY = "_UUID_";
	public static final String EX_KEY = "_EX_";
	public static final String VO_KEY = "_VO_";
	public static final String JT_KEY = "_JT_";
	public static final String DS_KEY = "_DS_";
	public static final String SEQ_KEY = "_SEQUENCE_";
	public static final String FILE_ALL_KEY = "_FILE_ALL_";
	public static final String FILE_FIELDS_KEY = "_FILE_FIELDS_";
	public static final String ASSIGNED_FIELDS_KEY = "_ASSIGNED_FIELDS_";
	public static final String DB_TYPE_KEY = "_DB_TYPE_";
	public static final String UPDATE_NULL_KEY = "_UPDATE_NULL_";
	public static final String SELECT_ATTACH_TAIL_KEY = "_SELECT_TAIL_"; // select������ĸ������
	public static final String UPDATE_ATTACH_TAIL_KEY = "_UPDATE_TAIL_"; // update������ĸ������
	public static final String DELETE_ATTACH_TAIL_KEY = "_DELETE_TAIL_"; // delete������ĸ������
	public static final String TABLE_SEQUENCE_SQL_ID = "common.talbeSequence";
}
