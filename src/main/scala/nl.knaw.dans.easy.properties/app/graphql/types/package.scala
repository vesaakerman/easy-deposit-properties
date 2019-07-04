package nl.knaw.dans.easy.properties.app.graphql

import sangria.schema.Context

package object types {
  
  private[types] implicit def dataContextFromContext(implicit ctx: Context[DataContext, _]): DataContext = ctx.ctx
}
