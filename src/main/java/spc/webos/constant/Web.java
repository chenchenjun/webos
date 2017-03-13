package spc.webos.constant;

public class Web
{
	// public final static String JSRMI_OBJ_CLASS = "_JSRMI_OBJECT_CLASS_"; //
	// JSRMI�ı�����

	// url��ַ��һЩ�̶�����
	public static final String REQ_SECURITY_GET_KEY = "_sget"; // get�����м���key
	public final static String REQ_KEY_UN_CLIENT_CACHE = "UN_CLIENT_CACHE"; // ����Ҫ�ͻ��˻���
	public final static String REQ_KEY_FORCE_FLUSH = "FORCE_FLUSH"; // ǿ�Ʒ���������ʧЧ
	public final static String REQ_KEY_EXT_DC = "_dc"; // ext��ֹ����ĵ�ַ�ظ��������������,
//	public final static String REQ_KEY_TOKEN = "TOKEN"; // ʹ��token����ʱ�� key
	public final static String REQ_KEY_LOGOUT = "_logout"; // �˳���ǰsession
	// ����һ��ʱ�������
	public final static String REQ_KEY_SEARCH_INDEX = "SEARCH_INDEX";
	public final static String REQ_KEY_FORM_SUCCESS_MODE = "SUCCESS_MODE";
	public final static String FORM_SUCCESS_MODE_JSON = "json";
	public final static String FORM_SUCCESS_MODE_PAGE = "page";
	public static final String REQ_KEY_VIEW_NAME_KEY = "viewName";
	public static final String REQ_KEY_VIEW_NAME_SKEY = "view";
	
	// ǰ��ָ��sql��ѯ���ж���
	public static final String REQ_KEY_SQL_CLASS = "SQL_CLASS";

	// public final static String REQ_KEY_ERR_PAGE = "ERR_PAGE"; // ����ҳ���ַ
	// public static final String REQ_KEY_EX_TYPE = "EX_TYPE"; // URLָ������ķ�������.
	// value=json, htm
	public static final String REQ_KEY_EX_MSG = "EX_MSG"; // �쳣��ʾ��Ϣ
	public static final String REQ_KEY_EX_CODE = "EX_CODE"; // �쳣��ʾ��Ϣ
	public static final String REQ_KEY_EX_LOC = "EX_LOC"; // �쳣λ��
	public static final String REQ_KEY_EX_TRC = "EX_TRC"; // �쳣������Ϣ
	// public static final String REQ_KEY_SERVICE_METHOD = "SERVICE_METHOD";
	public static final String REQ_KEY_SERVICE_DATA = "SERVICE_DATA";
	public static final String REQ_KEY_BATCH_SQL = "BATCH_SQL";
	public static final String REQ_KEY_BSQL = "bsid";
	public static final String REQ_PDFVIEW_NAME = "pdfView"; // pdfҳ������
	public static final String REQ_MSVIEW_NAME = "msView"; // microsoft ��һЩģ��,
	// ��������, word ,excel
	// ext grid�����һЩ����
	public static final String SERVICE_RET_KEY = "SVC_RET"; // ������ý������
	public static final String EXTGRID_DS_KEY = "GRID_DS"; // ext grid ds
	// public static final String EXTGRID_DS_SIZE_KEY = "GRID_SIZE"; // ext grid
	// ds
	// �ܼ�¼��
	// public static final String REQ_KEY_UTF8 = "UTF8";
	// public static final String REQ_KEY_TOTAL_SIZE_KEY_INMSG =
	// "TOTAL_SIZE_KEY";
	// public static final String REQ_KEY_RESULT_KEY_INMSG = "RESULT_KEY";
	public static final String REQ_KEY_COLUMN = "COLUMN"; // ���ڷ��񷵻ص�listÿ�е��ֶ���
	// public static final String REQ_KEY_TOTAL_SIZE = "TOTAL_SIZE"; //
	// ���ڷ���ʽ�����ļ�¼�ķ�ҳ��ʽ��������
	public static final String REQ_KEY_SQL = "sid"; // ��ѯ��sql id
	public static final String REQ_KEY_SIZE_SQL = "ssid"; // ��ѯ��ʽ�µ��ܼ�¼��ѯsql
	public static final String REQ_KEY_SQL_ID = "SQL_ID"; // ��ѯ��sql id
	public static final String REQ_KEY_SIZE_SQL_ID = "SIZE_SQL_ID"; // ��ѯ��ʽ�µ��ܼ�¼��ѯsql
	public static final String REQ_PAGING = "PAGING"; // ��������ҳ
	public static final String REQ_PAGING_DEF_LIMIT = "25";
	public static final String REQ_PAGING_ARGS_PAGE = "EXT_PAGE";
	public static final String REQ_PAGING_ARGS_START = "EXT_START";
	public static final String REQ_PAGING_ARGS_LIMIT = "EXT_LIMIT";
	public static final String REQ_EXTJS_PAGING_PAGE = "page";
	public static final String REQ_EXTJS_PAGING_START = "start";
	public static final String REQ_EXTJS_PAGING_LIMIT = "limit";
	public static final String REQ_KEY_UTF8 = "utf8"; // ת��Ϊutf8��ʽ\\u56\\u35

	// ��������
	public static final String REQ_KEY_FN = "FN"; // VO�е��ֶ���
	public static final String REQ_KEY_VO = "VO"; // VO��
	public static final String REQ_KEY_ZIP = "ZIP"; // �Ƿ�zip����
	// ��������
	public final static String REQ_KEY_VIEW_TYPE = "VIEW"; // view������
	// public final static String REQ_KEY_DOWNLOAD = "DOWNLOAD"; // ʹ������view����
	public final static String REQ_KEY_DOWNLOAD_FILE_NAME = "FILE_NAME"; // ��Ӧ�������ļ���ģ��ID
	public final static String REQ_KEY_DOWNLOAD_SUB_FILE_NAME = "SUB_FILE_NAME"; // ����������ļ�����,���ṩÿ�����ļ���,˳�����TEMPLATE_ID
	public final static String REQ_KEY_TEMPLATE_ID = "TEMPLATE_ID"; // ��Ӧ�������ļ���ģ��ID

	// json
	public static final String RETURN_TYPE_JSON = "json";

	public final static String POST_METHOD = "POST";
	public final static String GET_METHOD = "GET";
	public final static String RESP_ATTR_ERROR_KEY = "SERVER_ERR"; // ������������ʱ������response.setAttribute(RESP_ATTR_ERR_KEY,
	// Object);
	public final static int SERVER_EXCEPTION_STATUS_CODE = 600; // �������쳣�����ؿͻ���״̬�룬���ظ�ʽΪJson
	public final static int SERVER_JSON_STATUS_CODE = 601; // ���������ظ�ʽΪjson
	public final static int SERVER_XML_STATUS_CODE = 602; // ���������ظ�ʽΪxml
}
