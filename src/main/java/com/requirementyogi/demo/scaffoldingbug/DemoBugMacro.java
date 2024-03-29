package com.requirementyogi.demo.scaffoldingbug;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.core.ContextPathHolder;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.util.HtmlUtil;
import com.requirementyogi.demo.scaffoldingbug.rest.DemoBugResource;

import java.util.Map;

public class DemoBugMacro implements Macro {

    private final ContextPathHolder contextPathHolder;

    public DemoBugMacro(ContextPathHolder contextPathHolder) {
        this.contextPathHolder = contextPathHolder;
    }

    @Override
    public String execute(
        Map<String, String> map,
        String s,
        ConversionContext conversionContext
    ) throws MacroExecutionException {
        long pageId = conversionContext.getEntity().getId();
        String url = contextPathHolder.getContextPath() + "/rest/demobug/1/render?pageId=" + pageId;
        ConfluenceUser user = AuthenticatedUserThreadLocal.get();

        if (user == null) {
        return
            "\n<br/>>>>Start of macro" +

            "\n\n\n<br/>Rendered as the user: Anonymous" +

            "\n\n\n<br/><<< End of macro"
            ;
        }

        return
            "\n<br/>>>>Start of macro" +

            "\n\n\n<br/>Rendered as the user: " + user.getName() +

            "\n\n\n<br/>The bug=<a href=\"" + HtmlUtil.htmlEncode(url) + "\">Click here to see this macro rendered by the REST API</a>" +

            "\n<br/><<< End of macro"
            ;
    }

    @Override
    public BodyType getBodyType() {
        return BodyType.NONE;
    }

    @Override
    public OutputType getOutputType() {
        return OutputType.BLOCK;
    }
}
