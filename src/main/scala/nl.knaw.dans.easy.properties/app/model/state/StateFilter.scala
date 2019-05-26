package nl.knaw.dans.easy.properties.app.model.state

object StateFilter extends Enumeration {
  type StateFilter = Value

  // @formatter:off
  val LATEST: StateFilter = Value("LATEST")
  val ALL   : StateFilter = Value("ALL")
  // @formatter:on
}
