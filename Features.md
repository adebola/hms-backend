# HMS Patient Service - Comprehensive Feature List

## Overview

The Patient Service is a core microservice within the Hospital Management System (HMS) responsible for managing all patient-related data and operations. It serves as the central repository for patient demographics, registration, medical history, and provides search capabilities across the healthcare enterprise.

---

## 1. Patient Registration

### 1.1 New Patient Registration
- Capture complete patient demographics during initial registration
- Support for walk-in, scheduled, and emergency registration workflows
- Pre-registration capability for scheduled appointments
- Self-service registration via patient portal integration
- Barcode/QR code generation for patient identification bands

### 1.2 Medical Record Number (MRN) Management
- Auto-generation of unique MRN using configurable algorithms
- Support for multiple identifier types (MRN, National ID, Passport, Insurance ID)
- Cross-reference mapping between external identifiers
- MRN format validation per organizational rules

### 1.3 Registration Types
- Inpatient registration
- Outpatient registration
- Emergency registration (expedited workflow with minimal required fields)
- Newborn registration (linked to mother's record)
- Unknown/John Doe registration for unidentified patients

---

## 2. Patient Demographics

### 2.1 Core Demographics
- Full legal name (first, middle, last, suffix, prefix)
- Preferred name / alias management
- Date of birth with age calculation
- Gender (administrative and clinical gender identity)
- Marital status
- Nationality and citizenship
- Primary language and interpreter requirements
- Religion (optional, for chaplaincy services)
- Ethnicity and race (for clinical and reporting purposes)

### 2.2 Contact Information
- Multiple address support (home, work, temporary, billing)
- Address validation and standardization
- Multiple phone numbers with type classification (mobile, home, work)
- Email addresses with verification status
- Preferred contact method and best time to contact
- Communication preferences (SMS, email, phone, postal)

### 2.3 Emergency Contacts
- Multiple emergency contact records
- Relationship classification
- Priority ordering of contacts
- Contact availability notes

### 2.4 Next of Kin
- Legal next of kin designation
- Relationship documentation
- Power of attorney indicators
- Healthcare proxy designation

### 2.5 Employer Information
- Current employer details
- Occupation and job title
- Work contact information
- Occupational health linkage

---

## 3. Insurance & Financial Information

### 3.1 Insurance Coverage
- Multiple insurance plan support with priority ordering
- Primary, secondary, and tertiary payer management
- Policy number and group number capture
- Subscriber information (when patient is dependent)
- Coverage effective dates and termination tracking
- Insurance card image storage
- Real-time eligibility verification integration

### 3.2 Financial Classification
- Patient financial class assignment
- Self-pay identification
- Charity care eligibility flags
- Payment plan enrollment status
- Financial assistance program indicators

### 3.3 Guarantor Management
- Guarantor demographics capture
- Relationship to patient
- Billing address management
- Employer information for guarantor

---

## 4. Medical History

### 4.1 Problem List
- Active and resolved problem tracking
- ICD-10/ICD-11 coding integration
- SNOMED CT terminology support
- Problem onset dates and resolution dates
- Problem severity and status classification
- Problem annotations and clinical notes

### 4.2 Allergy & Adverse Reactions
- Drug allergies with severity classification
- Food allergies
- Environmental allergies
- Adverse drug reactions
- Allergy verification status (confirmed, suspected, refuted)
- Reaction type and manifestation documentation
- No known allergies (NKA) explicit documentation

### 4.3 Medication History
- Current medications list
- Historical medication records
- Medication reconciliation support
- External pharmacy integration
- Prescription drug monitoring program (PDMP) integration hooks
- Over-the-counter medication tracking
- Herbal/supplement documentation

### 4.4 Surgical History
- Past surgical procedures with dates
- Procedure coding (CPT, ICD-10-PCS)
- Surgical site and laterality
- Surgeon and facility information
- Complications documentation

### 4.5 Family History
- Family member health conditions
- Relationship classification
- Age of onset for conditions
- Genetic risk indicators
- Family history questionnaire support

### 4.6 Social History
- Tobacco use assessment
- Alcohol use assessment
- Substance use screening
- Sexual history (when clinically relevant)
- Exercise and diet patterns
- Living situation and social support
- Advance directive status

### 4.7 Immunization Records
- Vaccination history with dates
- CVX/MVX coding support
- Lot number and manufacturer tracking
- Immunization registry integration
- Vaccine refusal documentation
- Immunization forecasting integration

---

## 5. Clinical Information

### 5.1 Vital Signs History
- Historical vital signs repository
- Blood pressure, heart rate, respiratory rate
- Temperature, oxygen saturation
- Height, weight, BMI calculation
- Pain scores
- Vital signs trending and alerting

### 5.2 Lab Results Repository
- Historical laboratory results
- LOINC coding integration
- Reference range management
- Critical value flagging
- Result trending and graphing support

### 5.3 Diagnostic Reports
- Radiology report storage
- Pathology report storage
- Cardiology report storage
- Other diagnostic study results

### 5.4 Care Team
- Primary care provider assignment
- Specialist referral relationships
- Care team member listing
- Provider attribution for value-based care

---

## 6. Patient Search & Lookup

### 6.1 Search Capabilities
- MRN exact match search
- Name search (exact, partial, phonetic/Soundex)
- Date of birth search
- SSN/National ID search (with appropriate access controls)
- Phone number search
- Address search
- Insurance ID search
- Custom identifier search

### 6.2 Advanced Search
- Multi-criteria search with AND/OR logic
- Fuzzy matching for misspellings
- Wildcard search support
- Search result ranking by relevance
- Search within date ranges
- Search by registration date
- Search by last encounter date

### 6.3 Search Performance
- Elasticsearch/OpenSearch integration for fast full-text search
- Search result pagination
- Search result caching
- Search audit logging
- Saved search functionality

---

## 7. Patient Matching & Duplicate Management

### 7.1 Duplicate Detection
- Real-time duplicate checking during registration
- Probabilistic matching algorithms
- Configurable matching rules and thresholds
- Potential duplicate queue for manual review
- Duplicate score confidence levels

### 7.2 Patient Merge
- Merge workflow with approval process
- Surviving record selection
- Data element merge rules
- Merge audit trail
- Merge undo capability (within time window)
- Downstream system merge notification

### 7.3 Patient Unmerge
- Unmerge request workflow
- Record separation with data redistribution
- Unmerge audit documentation

### 7.4 Master Patient Index (MPI)
- Enterprise MPI integration
- Cross-facility patient linking
- External MPI federation support
- Golden record management

---

## 8. Consent & Privacy Management

### 8.1 Consent Documentation
- Treatment consent tracking
- Research consent management
- Information sharing consent
- Consent form versioning
- Electronic signature capture
- Consent expiration management

### 8.2 Privacy Preferences
- Information disclosure preferences
- Facility directory opt-out
- Restricted access patient flags (VIP, employee, celebrity)
- Break-the-glass access protocols
- Sensitive diagnosis protection

### 8.3 Advance Directives
- Living will documentation
- DNR/DNI status
- Healthcare proxy designation
- POLST/MOLST documentation
- Organ donor status

---

## 9. Document Management

### 9.1 Patient Documents
- Document upload and storage
- Document categorization and typing
- Document versioning
- Document metadata management
- OCR integration for scanned documents

### 9.2 Document Types
- Photo ID storage
- Insurance cards
- Signed consent forms
- External medical records
- Legal documents (POA, guardianship)
- Correspondence

### 9.3 Document Access
- Role-based document access control
- Document sharing with external providers
- Document expiration management
- Document retention policies

---

## 10. Multi-Tenancy & Organization

### 10.1 Multi-Tenant Architecture
- Organization-level data isolation
- Tenant-specific configuration
- Cross-tenant patient sharing (with consent)
- Tenant-specific MRN sequences
- Tenant-specific validation rules

### 10.2 Facility Management
- Multiple facility support per organization
- Facility-specific patient registration
- Patient transfer between facilities
- Facility-level access controls

### 10.3 Department Integration
- Department-level patient tracking
- Service line attribution
- Cost center association

---

## 11. Integration & Interoperability

### 11.1 HL7 FHIR R4 Support
- Patient resource (read, create, update, search)
- RelatedPerson resource
- Coverage resource
- AllergyIntolerance resource
- Condition resource
- FHIR subscription support for patient events

### 11.2 HL7 v2 Messaging
- ADT messages (A01, A02, A03, A04, A05, A08, etc.)
- ADT event broadcasting
- Inbound ADT processing

### 11.3 External System Integration
- Laboratory Information System (LIS) integration
- Radiology Information System (RIS) integration
- Pharmacy system integration
- Scheduling system integration
- Billing system integration
- Health Information Exchange (HIE) connectivity

### 11.4 API Capabilities
- RESTful API for all patient operations
- GraphQL API for complex queries (optional)
- Webhook support for patient events
- API versioning
- Rate limiting and throttling

---

## 12. Audit & Compliance

### 12.1 Audit Logging
- All patient data access logging
- Data modification audit trail
- User identification in audit records
- Timestamp and source system tracking
- Audit log retention per compliance requirements

### 12.2 Compliance Features
- HIPAA compliance support
- GDPR data subject rights (where applicable)
- Right to access / data export
- Right to erasure (with clinical data retention rules)
- Data portability
- Minimum necessary access enforcement

### 12.3 Reporting
- Patient census reporting
- Registration statistics
- Demographic analytics
- Data quality metrics
- Compliance audit reports

---

## 13. Data Quality & Governance

### 13.1 Data Validation
- Required field enforcement
- Format validation (phone, email, postal code)
- Cross-field validation rules
- Address verification services
- Duplicate data prevention

### 13.2 Data Standardization
- Name standardization (capitalization, formatting)
- Address standardization (USPS/postal service integration)
- Phone number formatting
- Code set validation

### 13.3 Data Completeness
- Completeness scoring per patient record
- Missing data identification
- Data quality dashboards
- Data remediation workflows

---

## 14. Notifications & Alerts

### 14.1 Patient Alerts
- Allergy alerts on record access
- Restricted patient access alerts
- Duplicate patient warnings
- Missing required data alerts
- Insurance eligibility alerts

### 14.2 System Notifications
- Registration confirmation notifications
- Appointment reminders (integration hook)
- Data update notifications
- Consent expiration reminders

### 14.3 Event Publishing
- Patient created events
- Patient updated events
- Patient merged events
- Demographics changed events
- RabbitMQ/Kafka message publishing

---

## 15. Technical Requirements

### 15.1 Performance
- Sub-second search response times
- Support for 10,000+ concurrent users
- Horizontal scaling capability
- Database read replica support
- Query optimization and indexing

### 15.2 Availability
- 99.9% uptime SLA target
- Health check endpoints
- Graceful degradation
- Circuit breaker patterns
- Retry mechanisms

### 15.3 Security
- OAuth2/OIDC authentication (via Auth Server)
- Role-based access control (RBAC)
- Field-level security
- Data encryption at rest
- TLS encryption in transit
- SQL injection prevention
- Input sanitization

### 15.4 Observability
- Structured logging
- Distributed tracing (OpenTelemetry)
- Metrics collection (Prometheus)
- Health and readiness probes
- Performance monitoring

---

## 16. Administrative Functions

### 16.1 Configuration Management
- Configurable required fields per registration type
- Custom field definitions
- Validation rule configuration
- Code set management (marital status, religion, etc.)
- Workflow configuration

### 16.2 User Preferences
- Default search preferences
- Display preferences
- Notification preferences
- Workflow shortcuts

### 16.3 Batch Operations
- Bulk patient import
- Data migration utilities
- Mass update capabilities
- Data export for analytics

---

## Appendix A: Entity Relationships

```
Patient
├── Demographics
├── Addresses (1:N)
├── Phone Numbers (1:N)
├── Email Addresses (1:N)
├── Emergency Contacts (1:N)
├── Insurance Coverages (1:N)
├── Guarantors (1:N)
├── Allergies (1:N)
├── Problems (1:N)
├── Medications (1:N)
├── Surgical History (1:N)
├── Family History (1:N)
├── Social History (1:1)
├── Immunizations (1:N)
├── Consents (1:N)
├── Documents (1:N)
├── Identifiers (1:N)
└── Audit Logs (1:N)
```

---

## Appendix B: Integration Points

| System | Direction | Protocol | Purpose |
|--------|-----------|----------|---------|
| Auth Server | Inbound | OAuth2/OIDC | Authentication & Authorization |
| API Gateway | Inbound | REST/gRPC | API routing |
| Scheduling Service | Bidirectional | REST/Events | Appointment context |
| Encounter Service | Outbound | Events | Patient context for visits |
| Billing Service | Outbound | Events | Insurance & demographic sync |
| Laboratory Service | Bidirectional | HL7v2/FHIR | Results & orders |
| Pharmacy Service | Bidirectional | REST/Events | Medication reconciliation |
| Notification Service | Outbound | RabbitMQ | Patient notifications |
| Reporting Service | Outbound | Events/CDC | Analytics & reporting |

---

## Appendix C: Suggested Phased Implementation

### Phase 1: Core Registration
- Basic patient demographics
- MRN generation
- Patient search
- Multi-tenant support

### Phase 2: Extended Demographics
- Insurance management
- Emergency contacts
- Guarantor management
- Document storage

### Phase 3: Clinical History
- Allergy management
- Problem list
- Medication history
- Immunizations

### Phase 4: Advanced Features
- Duplicate detection/merge
- MPI integration
- FHIR API
- Advanced search

### Phase 5: Optimization
- Performance tuning
- Advanced analytics
- AI-assisted data quality
- Predictive features

---

*Document Version: 1.0*  
*Last Updated: January 2026*  
*Author: HMS Development Team*
