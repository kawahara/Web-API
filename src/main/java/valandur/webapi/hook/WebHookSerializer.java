package valandur.webapi.hook;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.slf4j.Logger;
import valandur.webapi.WebAPI;
import valandur.webapi.api.hook.BaseWebHookFilter;
import valandur.webapi.api.hook.WebHookHeader;
import valandur.webapi.api.permission.IPermissionService;
import valandur.webapi.api.util.TreeNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WebHookSerializer implements TypeSerializer<WebHook> {

    @Override
    public WebHook deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        Logger logger = WebAPI.getLogger();

        String address = value.getNode("address").getString();

        if (address == null) {
            logger.error("No address specified for web hook!");
            return null;
        }

        logger.info("  - " + address);

        boolean enabled = value.getNode("enabled").getBoolean();
        WebHook.WebHookMethod method = value.getNode("method").getValue(TypeToken.of(WebHook.WebHookMethod.class));
        WebHook.WebHookDataType dataType = value.getNode("dataType").getValue(TypeToken.of(WebHook.WebHookDataType.class));
        boolean form = value.getNode("form").getBoolean();
        List<WebHookHeader> headers = value.getNode("headers").getList(TypeToken.of(WebHookHeader.class));
        boolean details = value.getNode("details").getBoolean();

        ConfigurationNode filterBase = value.getNode("filter");
        String filterName = filterBase.getNode("name").getString();
        ConfigurationNode filterConfig = filterBase.getNode("config");

        TreeNode<String, Boolean> permissions = IPermissionService.permitAllNode();

        if (!enabled) {
            logger.info("    -> Disabled");
        } else {
            if (headers == null) {
                headers = new ArrayList<>();
            }

            if (method == null) {
                method = WebHook.WebHookMethod.POST;
                logger.warn("    Does not specify 'method', defaulting to 'POST'");
            }

            if (dataType == null) {
                dataType = WebHook.WebHookDataType.JSON;
                logger.warn("    Does not specify 'dataType', defaulting to 'JSON'");
            }

            if (value.getNode("permissions").isVirtual()) {
                logger.warn("    Does not specify 'permissions', defaulting to '*'");
            } else {
                permissions = WebAPI.getPermissionService().permissionTreeFromConfig(value.getNode("permissions"));
            }
        }

        WebHook hook = new WebHook(address, enabled, method, dataType, form, headers, details, permissions);

        if (enabled) {
            if (filterName != null) {
                Optional<Class<? extends BaseWebHookFilter>> opt = WebAPI.getWebHookService().getFilter(filterName);
                if (!opt.isPresent()) {
                    logger.error("    Could not find filter with name '" + filterName + "'");
                } else {
                    try {
                        Constructor ctor = opt.get().getConstructor(WebHook.class, ConfigurationNode.class);
                        hook.setFilter((BaseWebHookFilter) ctor.newInstance(hook, filterConfig));
                    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        logger.error("    Could not setup filter '" + filterName + "': " + e.getMessage());
                    }
                }
            }

            logger.info("    -> Ok");
        }

        return hook;
    }

    @Override
    public void serialize(TypeToken<?> type, WebHook obj, ConfigurationNode value) throws ObjectMappingException {
        value.getNode("address").setValue(obj.getAddress());
        value.getNode("enabled").setValue(obj.isEnabled());
        value.getNode("headers").setValue(obj.getHeaders());
        value.getNode("method").setValue(obj.getMethod());
        value.getNode("dataType").setValue(obj.getDataType());
        value.getNode("form").setValue(obj.isForm());
        value.getNode("details").setValue(obj.includeDetails());
        WebAPI.getPermissionService().permissionTreeToConfig(value.getNode("permissions"), obj.getPermissions());
    }
}
