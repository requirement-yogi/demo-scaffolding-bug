package com.requirementyogi.demo.scaffoldingbug.rest;

import com.atlassian.confluence.content.render.xhtml.BatchedRenderRequest;
import com.atlassian.confluence.content.render.xhtml.ConversionContextOutputType;
import com.atlassian.confluence.content.render.xhtml.DefaultConversionContext;
import com.atlassian.confluence.content.render.xhtml.view.BatchedRenderResult;
import com.atlassian.confluence.content.render.xhtml.view.RenderResult;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.plugins.rest.common.security.UnrestrictedAccess;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Path("/")
public class DemoBugResource {

    private final XhtmlContent xhtmlManager;
    private final PageManager pageManager;

    public DemoBugResource(XhtmlContent xhtmlManager, PageManager pageManager) {
        this.xhtmlManager = xhtmlManager;
        this.pageManager = pageManager;
    }

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML) // If there is an encoding issue, please add ";charset=utf-8" at the end
    @UnrestrictedAccess
    public Response info() {
        return Response.ok("OK! The plugin is correctly installed.").build();
    }

    @GET
    @Path("/render")
    @Produces(MediaType.TEXT_PLAIN) // If there is an encoding issue, please add ";charset=utf-8" at the end
    @UnrestrictedAccess
    public Response get(@QueryParam("pageId") Long pageId) {
        Page page = getPage(pageId);

        // Extract the contents of the first cell of the page
        String body = page.getBodyAsString();

        if (!body.contains("text-data")) {

        }

        String result = doInThread(
            () -> convertStorageToView(page, body)
        );

        return Response.ok(
             "=== RENDERING OF THE PAGE " + pageId + " === \n"
              + result
             + "\n\n\n=== Now look at the logs: There will be an exception ===\n"
        ).build();
    }

    private String convertStorageToView(Page page, String storageFormat) {
        PageContext renderContext = new PageContext(page);
        DefaultConversionContext conversionContext = new DefaultConversionContext(
            renderContext,
            ConversionContextOutputType.EMAIL.value()
        );
        BatchedRenderRequest request = new BatchedRenderRequest(
            conversionContext,
            List.of(storageFormat)
        );

        List<BatchedRenderResult> results = xhtmlManager.convertStorageToView(request);

        BatchedRenderResult result0 = results.get(0);
        RenderResult renderResult = result0.getResults().get(0);
        if (!renderResult.isSuccessful())
            return "RENDERING IS NOT SUCCESSFUL: " + renderResult.getRender();
        return renderResult.getRender();
    }

    /**
     * This method ensures that the context is clean:
     * - No logged-in user,
     * - No Http session in advance
     */
    private String doInThread(Supplier<String> callable) {
        AtomicReference<String> valueToReturn = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            String result = callable.get();
            valueToReturn.set(result);
        });

        try {
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        return valueToReturn.get();
    }

    private Page getPage(Long pageId) {
        if (pageId == null) {
            throw new WebApplicationException(
                Response
                .status(Response.Status.BAD_REQUEST)
                .entity("Missing parameter in URL: pageId=...\n\nHave you first created a page with the Scaffolding/Text Data macro?")
                .build());
        }
        Page page = pageManager.getPage(pageId);
        if (page == null) {
            throw new WebApplicationException(
                Response
                .status(Response.Status.BAD_REQUEST)
                .entity("The ?pageId=... doesn't match a page in the DB")
                .build()
            );
        }
        if (!page.getBodyAsString().contains("text-data")) {
            throw new WebApplicationException(
                Response
                .status(Response.Status.BAD_REQUEST)
                .entity("Please insert a \"text-data\" macro on the page and come back here.")
                .build()
            );
        }
        return page;
    }

    private static void checkItContainsTheMacro(String firstCell) {
        if (!firstCell.contains("text-data")) {
            throw new WebApplicationException(
                Response
                .status(Response.Status.BAD_REQUEST)
                .entity("Please put a table in the page, with one cell, containing the Text Data macro.")
                .build()
            );
        }
    }
}
