// FHIR Resource Types

export interface FhirHumanName {
  use?: string
  family: string | string[]
  given?: string[]
  prefix?: string[]
  suffix?: string[]
  period?: { start?: string; end?: string }
}

export interface FhirIdentifier {
  value: string
  system?: string
  use?: string
}

export interface FhirPersonResource {
  resourceType: string
  id: string
  name: FhirHumanName[]
  birthDate: string
  gender: string
  identifier?: FhirIdentifier[]
}

export interface FhirPatientResource extends FhirPersonResource {
  resourceType: 'Patient'
}

export interface FhirPatientEntry {
  resource: FhirPatientResource
}

export interface FhirPractitionerResource extends FhirPersonResource {
  resourceType: 'Practitioner'
}

export interface FhirPractitionerEntry {
  resource: FhirPractitionerResource
}

export interface FhirBundleLink {
  relation: string
  url: string
}

export interface FhirPatientBundle {
  resourceType: 'Bundle'
  total: number
  link: FhirBundleLink[]
  entry: FhirPatientEntry[]
}

export interface FhirPractitionerBundle {
  resourceType: 'Bundle'
  total: number
  link: FhirBundleLink[]
  entry: FhirPractitionerEntry[]
}

// SMART App / Sandbox Types

export interface SmartAppSandbox {
  sandboxId: string
}

export interface SmartApp {
  id: string
  clientName: string
  launchUri: string
  sandbox: SmartAppSandbox
}

export interface SmartLaunchContextProperties {
  clientId?: string
  patientId?: string
  encounterId?: string
  fhirUser?: string
}

// Persona Types

export interface UserPersona {
  id: number
  personaName: string
  personaUserId: string
  password: string
  fhirId: string
  resourceUrl: string
}

// Launch / Cookie Data

export interface ContextParam {
  name: string
  value: string
}

export interface LaunchData {
  bearerToken: string
  csrfToken?: string
  sandboxApiUrl: string
  sandboxId: string
  fhirApi: string
  personaId?: number
  patientId?: string
  appId?: string
  encounter?: string
  location?: string
  resource?: string
  smartStyleUrl?: string
  intent?: string
  contextParams?: ContextParam[]
}

// CDS Hooks Types

export interface CdsCardSource {
  label: string
  url?: string
}

export interface CdsCardSuggestion {
  label: string
  actions?: unknown[]
}

export interface CdsCardLink {
  label: string
  url: string
  type: 'absolute' | 'smart'
  appContext?: string
}

export interface CdsFhirAuthorization {
  access_token: string
  token_type: string
  scope: string
  subject: string
}

export interface CdsHookRequest {
  hookInstance: string
  hook: string
  fhirServer: string
  context: Record<string, string>
  fhirAuthorization: CdsFhirAuthorization
  prefetch: Record<string, unknown>
}

export interface CdsCard {
  summary: string
  indicator: 'info' | 'warning' | 'critical' | 'success'
  detail?: string
  source?: CdsCardSource
  suggestions?: CdsCardSuggestion[]
  links?: CdsCardLink[]
  requestData?: CdsHookRequest
  noCardsReturned?: boolean
}

// App State Types

export interface LaunchParams {
  patient?: string
  need_patient_banner?: boolean
  encounter?: string
  location?: string
  resource?: string
  smartStyleUrl?: string
  intent?: string
  [key: string]: string | boolean | undefined
}
