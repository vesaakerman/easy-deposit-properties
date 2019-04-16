/**
 * Copyright (C) 2019 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easy.properties.server

import org.scalatra.{ Ok, ScalatraServlet }

class GraphiQLServlet extends ScalatraServlet {
  
  get("/") {
    contentType = "text/html"
    Ok {
      """<html lang="en">
        |<head>
        |    <meta charset="utf-8">
        |    <meta http-equiv="X-UA-Compatible" content="IE=edge">
        |    <meta name="viewport" content="width=device-width, initial-scale=1">
        |
        |    <title>GraphiQL</title>
        |
        |    <link href="//cdn.jsdelivr.net/npm/graphiql@0.13.0/graphiql.css" rel="stylesheet" />
        |
        |    <script src="//cdn.jsdelivr.net/fetch/0.9.0/fetch.min.js"></script>
        |    <script src="//cdn.jsdelivr.net/npm/react@16.8.6/umd/react.production.min.js"></script>
        |    <script src="//cdn.jsdelivr.net/npm/react-dom@16.8.6/umd/react-dom.production.min.js"></script>
        |    <script src="//cdn.jsdelivr.net/npm/graphiql@0.13.0/graphiql.min.js"></script>
        |    <script src="//cdnjs.cloudflare.com/ajax/libs/jquery/3.4.0/jquery.min.js"></script>
        |
        |    <style>
        |      body {
        |        height: 100%;
        |        margin: 0;
        |        width: 100%;
        |        overflow: hidden;
        |      }
        |      #graphiql {
        |        height: 100vh;
        |      }
        |    </style>
        |
        |    <script type="text/javascript">
        |        $(function (global) {
        |          // Parse the search string to get url parameters.
        |          var search = window.location.search;
        |          var parameters = {};
        |          search.substr(1).split('&').forEach(function (entry) {
        |            var eq = entry.indexOf('=');
        |            if (eq >= 0) {
        |              parameters[decodeURIComponent(entry.slice(0, eq))] =
        |                  decodeURIComponent(entry.slice(eq + 1));
        |            }
        |          });
        |          // if variables was provided, try to format it.
        |          if (parameters.variables) {
        |            try {
        |              parameters.variables =
        |                  JSON.stringify(JSON.parse(query.variables), null, 2);
        |            } catch (e) {
        |              // Do nothing
        |            }
        |          }
        |          // When the query and variables string is edited, update the URL bar so
        |          // that it can be easily shared
        |          function onEditQuery(newQuery) {
        |            parameters.query = newQuery;
        |            updateURL();
        |          }
        |          function onEditVariables(newVariables) {
        |            parameters.variables = newVariables;
        |            updateURL();
        |          }
        |          function updateURL() {
        |            var newSearch = '?' + Object.keys(parameters).map(function (key) {
        |                  return encodeURIComponent(key) + '=' +
        |                      encodeURIComponent(parameters[key]);
        |                }).join('&');
        |            history.replaceState(null, null, newSearch);
        |          }
        |          // Defines a GraphQL fetcher using the fetch API.
        |          function graphQLFetcher(graphQLParams) {
        |            return fetch(window.location.origin + '/graphql', {
        |              method: 'post',
        |              headers: {
        |                'Accept': 'application/json',
        |                'Content-Type': 'application/json'
        |              },
        |              body: JSON.stringify(graphQLParams),
        |              credentials: 'include'
        |            }).then(function (response) {
        |              return response.text();
        |            }).then(function (responseBody) {
        |              try {
        |                return JSON.parse(responseBody);
        |              } catch (error) {
        |                return responseBody;
        |              }
        |            });
        |          }
        |          function setupZoom(percent) {
        |            $('html > head').append($('<style>body {zoom: ' + percent + '%;}</style>'))
        |          }
        |          if (parameters['zoom']) {
        |            setupZoom(parameters['zoom'])
        |          }
        |          if (parameters["hideVariables"]) {
        |            $('html > head').append($('<style>.variable-editor {display: none !important}</style>'))
        |          }
        |          global.renderGraphiql = function (elem) {
        |            // Render <GraphiQL /> into the body.
        |            ReactDOM.render(
        |                React.createElement(GraphiQL, {
        |                  fetcher: graphQLFetcher,
        |                  schema: undefined,
        |                  query: parameters.query,
        |                  variables: parameters.variables,
        |                  response: parameters.response,
        |                  onEditQuery: onEditQuery,
        |                  onEditVariables: onEditVariables
        |                }),
        |                elem
        |            );
        |          }
        |        }(window))
        |    </script>
        |  </head>
        |
        |  <body>
        |    <div id="graphiql">Loading...</div>
        |
        |    <script>
        |      renderGraphiql(document.getElementById('graphiql'))
        |    </script>
        |  </body>
        |</html>""".stripMargin
    }
  }
}
