package nl.knaw.dans.easy.properties.app.repository

case class Repository(deposits: DepositDao,
                      states: StateDao,
                      ingestSteps: IngestStepDao,
                      identifiers: IdentifierDao,
                      doiRegistered: DoiRegisteredDao,
                      doiAction: DoiActionDao,
                      curation: CurationDao,
                      springfield: SpringfieldDao,
                      contentType: ContentTypeDao,
                     )
