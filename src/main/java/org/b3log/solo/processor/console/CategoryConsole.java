/*
 * Copyright (c) 2010-2017, b3log.org & hacpai.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.solo.processor.console;

import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.JSONRenderer;
import org.b3log.latke.util.Requests;
import org.b3log.solo.model.Category;
import org.b3log.solo.service.CategoryMgmtService;
import org.b3log.solo.util.QueryResults;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Category console request processing.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Mar 31, 2017
 * @since 2.0.0
 */
@RequestProcessor
public class CategoryConsole {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CategoryConsole.class);

    /**
     * Category management service.
     */
    @Inject
    private CategoryMgmtService categoryMgmtService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Adds a category with the specified request.
     * <p>
     * Renders the response with a json object, for example,
     * <pre>
     * {
     *     "sc": boolean,
     *     "oId": "", // Generated category id
     *     "msg": ""
     * }
     * </pre>
     * </p>
     *
     * @param request  the specified http servlet request,
     *                 "categoryTitle": "",
     *                 "categoryURI": "", // optional
     *                 "categoryDescription": "", // optional
     *                 "categoryOrder": "", // optional, uses 10 instead if not specified
     * @param response the specified http servlet response
     * @param context  the specified http request context
     * @throws Exception exception
     */
    @RequestProcessing(value = "/console/category/", method = HTTPRequestMethod.POST)
    public void addCategory(final HttpServletRequest request, final HttpServletResponse response, final HTTPRequestContext context)
            throws Exception {
        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);

        final JSONObject ret = new JSONObject();
        renderer.setJSONObject(ret);

        try {
            final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);
            final String title = requestJSONObject.optString(Category.CATEGORY_TITLE, "Category");
            final String uri = requestJSONObject.optString(Category.CATEGORY_URI, "/Category");
            final String desc = requestJSONObject.optString(Category.CATEGORY_DESCRIPTION);
            final int order = requestJSONObject.optInt(Category.CATEGORY_ORDER);

            final JSONObject category = new JSONObject();
            category.put(Category.CATEGORY_TITLE, title);
            category.put(Category.CATEGORY_URI, uri);
            category.put(Category.CATEGORY_DESCRIPTION, desc);
            category.put(Category.CATEGORY_ORDER, order);

            final String categoryId = categoryMgmtService.addCategory(category);

            ret.put(Keys.OBJECT_ID, categoryId);
            ret.put(Keys.MSG, langPropsService.get("addSuccLabel"));
            ret.put(Keys.STATUS_CODE, true);
        } catch (final ServiceException e) {
            LOGGER.log(Level.ERROR, e.getMessage(), e);

            final JSONObject jsonObject = QueryResults.defaultResult();

            renderer.setJSONObject(jsonObject);
            jsonObject.put(Keys.MSG, e.getMessage());
        }
    }
}
