package com.dp.mvcframework.webmvc.servlet;

import java.io.File;

/**
 * Created by Tom.
 */
public class DPViewResolver {
    private final String DEFAULT_TEMPLATE_SUFFIX = ".html";
    private File tempateRootDir;

    public DPViewResolver(String templateRoot) {
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        tempateRootDir = new File(templateRootPath);
    }

    public DPView resolveViewName(String viewName) {
        if (null == viewName || "".equals(viewName.trim())) {
            return null;
        }
        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFIX) ? viewName : (viewName + DEFAULT_TEMPLATE_SUFFIX);
        File templateFile = new File((tempateRootDir.getPath() + "/" + viewName).replaceAll("/+", "/"));
        return new DPView(templateFile);
    }

}
