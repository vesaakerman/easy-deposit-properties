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

class GraphiQLServlet(backendPath: String) extends ScalatraServlet {

  get("/") {
    /*
     * this html response was copied from
     *     https://github.com/graphql/graphiql/blob/master/example/index.html
     *
     * after which the following changes were made:
     *   - add meta elements, title and favicon.ico to <head>
     *   - upgrade graphiql, react and react-dom libraries
     *   - removed unnecessary comments
     *   - add property 'schema: undefined' to the rendered GraphiQL Component
     *   - add property 'response: parameters.response' to the rendered GraphiQL Component
     */
    contentType = "text/html"
    Ok {
      s"""|<!DOCTYPE html>
          |<html lang="en">
          |<head>
          |    <meta charset="utf-8">
          |    <meta http-equiv="X-UA-Compatible" content="IE=edge">
          |    <meta name="viewport" content="width=device-width, initial-scale=1">
          |
          |    <title>GraphiQL</title>
          |
          |    <link rel="shortcut icon" type="image/x-icon" href="https://easy.dans.knaw.nl/ui/images/lay-out/favicon.ico">
          |    <link href="//cdn.jsdelivr.net/npm/graphiql@0.13.0/graphiql.css" rel="stylesheet" />
          |
          |    <script src="//cdn.jsdelivr.net/es6-promise/4.0.5/es6-promise.auto.min.js"></script>
          |    <script src="//cdn.jsdelivr.net/fetch/0.9.0/fetch.min.js"></script>
          |    <script src="//cdn.jsdelivr.net/npm/react@16.8.6/umd/react.production.min.js"></script>
          |    <script src="//cdn.jsdelivr.net/npm/react-dom@16.8.6/umd/react-dom.production.min.js"></script>
          |    <script src="//cdn.jsdelivr.net/npm/create-react-class@15.6.3/create-react-class.min.js"></script>
          |    <script src="//cdn.jsdelivr.net/npm/graphiql@0.13.2/graphiql.min.js"></script>
          |    <script src="//cdn.jsdelivr.net/npm/graphiql-explorer@0.4.3/graphiqlExplorer.min.js"></script>
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
          |      .graphiql-container {
          |        height: 100vh;
          |        width: 100vw;
          |      }
          |    </style>
          |</head>
          |<body>
          |    <div id="root">Loading...</div>
          |
          |    <script type="text/javascript">
          |      // Parse the search string to get url parameters.
          |      var search = window.location.search;
          |      var parameters = {};
          |      search.substr(1).split('&').forEach(function (entry) {
          |        var eq = entry.indexOf('=');
          |        if (eq >= 0) {
          |          parameters[decodeURIComponent(entry.slice(0, eq))] =
          |            decodeURIComponent(entry.slice(eq + 1));
          |        }
          |      });
          |      // if variables was provided, try to format it.
          |      if (parameters.variables) {
          |        try {
          |          parameters.variables =
          |            JSON.stringify(JSON.parse(parameters.variables), null, 2);
          |        } catch (e) {
          |          // Do nothing, we want to display the invalid JSON as a string, rather
          |          // than present an error.
          |        }
          |      }
          |      // When the query and variables string is edited, update the URL bar so
          |      // that it can be easily shared
          |      function onEditQuery(newQuery) {
          |        parameters.query = newQuery;
          |        updateURL();
          |      }
          |      function onEditVariables(newVariables) {
          |        parameters.variables = newVariables;
          |        updateURL();
          |      }
          |      function onEditOperationName(newOperationName) {
          |        parameters.operationName = newOperationName;
          |        updateURL();
          |      }
          |      function updateURL() {
          |        var newSearch = '?' + Object.keys(parameters).filter(function (key) {
          |          return Boolean(parameters[key]);
          |        }).map(function (key) {
          |          return encodeURIComponent(key) + '=' +
          |            encodeURIComponent(parameters[key]);
          |        }).join('&');
          |        history.replaceState(null, null, newSearch);
          |      }
          |      function graphQLFetcher(graphQLParams) {
          |        return fetch('http://localhost:20200/graphql', {
          |          method: 'post',
          |          headers: {
          |            'Accept': 'application/json',
          |            'Content-Type': 'application/json',
          |          },
          |          body: JSON.stringify(graphQLParams),
          |          credentials: 'include',
          |        }).then(function (response) {
          |          return response.text();
          |        }).then(function (responseBody) {
          |          try {
          |            return JSON.parse(responseBody);
          |          } catch (error) {
          |            return responseBody;
          |          }
          |        });
          |      }
          |
          |      function bindGraphiQLRef(ref) {
          |        parameters._graphiql = ref;
          |      }
          |
          |      const App = createReactClass({
          |        getInitialState: function() {
          |          return {
          |            explorerIsOpen: true,
          |            schema: undefined,
          |            query: null,
          |          };
          |        },
          |        
          |        componentDidMount: function() {
          |          console.log("state", this.state);
          |          console.log("graphql component", parameters._graphiql);
          |          console.log("graphql component state", parameters._graphiql.state);
          |          
          |          if (!this.state.schema) {
          |            console.log("set schema from graphql object");
          |            const schema = parameters._graphiql.state.schema;
          |            schema && this.setState({ schema });
          |          }
          |        },
          |
          |        _handleEditQuery: function(query) {
          |          parameters.query = query
          |          updateURL()
          |          this.setState({ query });
          |        },
          |
          |        _handleToggleExplorer: function() {
          |          console.log("graphql component state", parameters._graphiql.state);
          |          
          |          const schema = this.state.schema || parameters._graphiql.state.schema
          |          
          |          const newExplorerIsOpen = !this.state.explorerIsOpen
          |          parameters.explorerIsOpen = newExplorerIsOpen
          |          updateURL()
          |          this.setState({ schema: schema, explorerIsOpen: newExplorerIsOpen });
          |        },
          |
          |        render: function() {
          |          const { query, schema, explorerIsOpen } = this.state;
          |          console.log("schema", schema);
          |          return React.createElement('div', { className: 'graphiql-container' },
          |            React.createElement(GraphiQLExplorer.Explorer, {
          |              schema: schema,
          |              query: query,
          |              onEdit: this._handleEditQuery,
          |              onRunOperation: (operationName) => parameters._graphiql.handleRunQuery(operationName),
          |              explorerIsOpen: explorerIsOpen,
          |              onToggleExplorer: this._handleToggleExplorer,
          |            }),
          |            React.createElement(GraphiQL, {
          |              ref: bindGraphiQLRef,
          |              fetcher: graphQLFetcher,
          |              schema: schema,
          |              query: query,
          |              variables: parameters.variables,
          |              response: parameters.response,
          |              operationName: parameters.operationName,
          |              onEditQuery: this._handleEditQuery,
          |              onEditVariables: onEditVariables,
          |              onEditOperationName: onEditOperationName
          |            },
          |              React.createElement(GraphiQL.Toolbar, {},
          |                React.createElement(GraphiQL.Button, {
          |                  label: "Prettify",
          |                  title: "Prettify Query (Shift-Ctrl-P)",
          |                  onClick: () => parameters._graphiql.handlePrettifyQuery(),
          |                }),
          |                React.createElement(GraphiQL.Button, {
          |                  label: "Merge",
          |                  title: "Merge Query (Shift-Ctrl-M)",
          |                  onClick: () => parameters._graphiql.handleMergeQuery(),
          |                }),
          |                React.createElement(GraphiQL.Button, {
          |                  label: "History",
          |                  title: "Show History",
          |                  onClick: () => parameters._graphiql.handleToggleHistory(),
          |                }),
          |                React.createElement(GraphiQL.Button, {
          |                  label: "Explorer",
          |                  title: "Toggle Explorer",
          |                  onClick: this._handleToggleExplorer,
          |                }),
          |              ),
          |            ),
          |          );
          |        },
          |      });
          |
          |      ReactDOM.render(
          |        React.createElement(App, {}),
          |        document.getElementById('root'),
          |      );
          |    </script>
          |</body>
          |</html>""".stripMargin
    }
  }
}
