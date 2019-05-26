package nl.knaw.dans.easy.properties.app.model.state

import nl.knaw.dans.easy.properties.app.model.Timestamp
import nl.knaw.dans.easy.properties.app.model.state.StateLabel.StateLabel

case class State(id: String, label: StateLabel, description: String, timestamp: Timestamp)
