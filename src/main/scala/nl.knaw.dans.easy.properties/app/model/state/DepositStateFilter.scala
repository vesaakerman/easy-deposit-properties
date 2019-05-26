package nl.knaw.dans.easy.properties.app.model.state

import nl.knaw.dans.easy.properties.app.model.DepositFilter
import nl.knaw.dans.easy.properties.app.model.state.StateFilter.StateFilter
import nl.knaw.dans.easy.properties.app.model.state.StateLabel.StateLabel

case class DepositStateFilter(label: StateLabel, filter: StateFilter = StateFilter.LATEST) extends DepositFilter
