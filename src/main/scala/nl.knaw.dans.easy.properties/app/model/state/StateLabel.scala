package nl.knaw.dans.easy.properties.app.model.state

object StateLabel extends Enumeration {
  type StateLabel = Value

  // @formatter:off
  val DRAFT          : StateLabel = Value("DRAFT")
  val UPLOADED       : StateLabel = Value("UPLOADED")
  val FINALIZING     : StateLabel = Value("FINALIZING")
  val INVALID        : StateLabel = Value("INVALID")
  val SUBMITTED      : StateLabel = Value("SUBMITTED")
  val REJECTED       : StateLabel = Value("REJECTED")
  val FAILED         : StateLabel = Value("FAILED")
  val IN_REVIEW      : StateLabel = Value("IN_REVIEW")
  val ARCHIVED       : StateLabel = Value("ARCHIVED")
  val FEDORA_ARCHIVED: StateLabel = Value("FEDORA_ARCHIVED")
  // @formatter:on
}
