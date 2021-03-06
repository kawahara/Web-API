package valandur.webapi.api.hook;

import valandur.webapi.api.util.TreeNode;

import java.util.List;

public interface IWebHook {

    enum WebHookMethod {
        GET, PUT, POST, DELETE
    }

    enum WebHookDataType {
        JSON, XML
    }


    String getAddress();

    boolean isEnabled();

    WebHookMethod getMethod();

    WebHookDataType getDataType();

    boolean isForm();

    String getDataTypeHeader();

    List<WebHookHeader> getHeaders();

    boolean includeDetails();

    TreeNode<String, Boolean> getPermissions();

    BaseWebHookFilter getFilter();

    void setFilter(BaseWebHookFilter filter);
}
