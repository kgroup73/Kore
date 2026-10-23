package com.kore.service;

import com.kore.model.AuditLogItem;
import com.kore.model.AutomationItem;
import com.kore.model.BpmWorkflowStep;
import com.kore.model.DocumentNode;
import com.kore.model.LegalContext;
import com.kore.model.OcrEntities;
import com.kore.model.RepositoryNode;
import com.kore.model.RepositoryQuota;
import com.kore.model.TaxonomyNode;
import com.kore.model.TelemetryData;
import com.kore.model.TrdMetadata;
import com.kore.model.UserProfile;
import com.kore.model.VersionItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MockDataService {

    public List<DocumentNode> createMockDocuments() {
        List<DocumentNode> docs = new ArrayList<>();

        // Documento 1 - Seleccionado por defecto en la vista
        DocumentNode doc1 = new DocumentNode(
                "CONTRATO-MARCO-CLOUD-2025-V2.pdf",
                "pdf",
                "okm:node-7b89-4fa2-91cd",
                "SHA-256 e3b0c442",
                "v2.4",
                "Bloqueado",
                "EXP-2025-CORP-482",
                "#789012",
                4404019L,
                "4.2 MB",
                "application/pdf",
                "Registro en WAL: Activo (Tx# 90812)",
                "Check-in (v2.5)",
                "contratos_2025",
                true,
                new TrdMetadata(
                        "210.04 Contratación",
                        "5 años (Archivo Central)",
                        "Conservación Total (CT)",
                        "Nivel 3 - Restringido"
                ),
                new OcrEntities(
                        "99.4% Precisión",
                        "900.245.112-1 (AWS Cloud LatAm)",
                        "$185,000.00 USD",
                        "01/01/2025 - 31/12/2027",
                        "Cámara de Comercio de Bogotá"
                ),
                List.of(
                        new AutomationItem("validador_trd.py", AutomationItem.AutomationType.SCRIPT, "Ejecutado OK", true),
                        new AutomationItem("Webhook SAP ERP (MM)", AutomationItem.AutomationType.WEBHOOK, "Sincronizado", true)
                ),
                new TelemetryData(
                        "Últimos 30 días",
                        148,
                        List.of(15.0, 22.0, 30.0, 26.0, 48.0, 56.0, 52.0, 72.0, 88.0, 94.0, 115.0, 134.0, 148.0),
                        "https://app.powerbi.com/groups/me/reports/kore-contracts"
                )
        );
        docs.add(doc1);

        // Documento 2
        DocumentNode doc2 = new DocumentNode(
                "ACUERDO_CONFIDENCIALIDAD_NDA_SLA.docx",
                "docx",
                "okm:node-9c12-3be1-884a",
                "SHA-256 a1b2c3d4",
                "v1.2",
                "Disponible",
                "EXP-2025-CORP-483",
                "#789013",
                1887436L,
                "1.8 MB",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "Registro en WAL: Activo (Tx# 90813)",
                "Check-out (v1.3)",
                "contratos_2025",
                false,
                new TrdMetadata(
                        "210.01 Acuerdos y Convenios",
                        "10 años (Archivo Central)",
                        "Selección (S)",
                        "Nivel 2 - Confidencial"
                ),
                new OcrEntities(
                        "98.1% Precisión",
                        "830.098.321-4 (Cloud SLA Partners)",
                        "$45,000.00 USD",
                        "15/02/2025 - 15/02/2026",
                        "Cámara de Comercio de Medellín"
                ),
                List.of(
                        new AutomationItem("validador_trd.py", AutomationItem.AutomationType.SCRIPT, "Ejecutado OK", true),
                        new AutomationItem("Webhook SAP ERP (MM)", AutomationItem.AutomationType.WEBHOOK, "Sincronizado", true)
                ),
                new TelemetryData(
                        "Últimos 30 días",
                        64,
                        List.of(8.0, 12.0, 18.0, 25.0, 22.0, 38.0, 44.0, 52.0, 58.0, 64.0),
                        "https://app.powerbi.com/groups/me/reports/kore-nda"
                )
        );
        docs.add(doc2);

        // Documento 3
        DocumentNode doc3 = new DocumentNode(
                "EXPEDIENTE_AUDITORIA_Q3_ACTA.pdf",
                "pdf",
                "okm:node-44ef-7a91-0021",
                "SHA-256 f5e4d3c2",
                "v3.0",
                "En Revisión",
                "EXP-2025-AUD-104",
                "#789014",
                8912896L,
                "8.5 MB",
                "application/pdf",
                "Registro en WAL: Activo (Tx# 90814)",
                "Check-in (v3.1)",
                "auditorias",
                false,
                new TrdMetadata(
                        "100.12 Informes de Auditoría",
                        "20 años (Archivo Histórico)",
                        "Conservación Total (CT)",
                        "Nivel 4 - Secreto Corporativo"
                ),
                new OcrEntities(
                        "99.8% Precisión",
                        "860.001.234-9 (PwC Auditores)",
                        "N/A (Acta Administrativa)",
                        "01/07/2025 - 30/09/2025",
                        "Bogotá D.C."
                ),
                List.of(
                        new AutomationItem("validador_trd.py", AutomationItem.AutomationType.SCRIPT, "Ejecutado OK", true),
                        new AutomationItem("Webhook SAP ERP (MM)", AutomationItem.AutomationType.WEBHOOK, "Sincronizado", true)
                ),
                new TelemetryData(
                        "Últimos 30 días",
                        312,
                        List.of(45.0, 78.0, 92.0, 118.0, 155.0, 205.0, 238.0, 275.0, 312.0),
                        "https://app.powerbi.com/groups/me/reports/kore-audit"
                )
        );
        docs.add(doc3);

        // Documento 4
        DocumentNode doc4 = new DocumentNode(
                "FACTURA_ELECTRONICA_PROVEEDOR_SERV_4891.xml",
                "xml",
                "okm:node-11df-55ce-3301",
                "SHA-256 99b8a7f6",
                "v1.0",
                "Disponible",
                "EXP-2025-FIN-992",
                "#789015",
                348160L,
                "340 KB",
                "application/xml",
                "Registro en WAL: Activo (Tx# 90815)",
                "Check-out (v1.1)",
                "proveedores",
                false,
                new TrdMetadata(
                        "300.08 Facturación Electrónica",
                        "5 años (Archivo de Gestión)",
                        "Eliminación (E)",
                        "Nivel 1 - Interno"
                ),
                new OcrEntities(
                        "100% Estructurado",
                        "901.445.678-0 (Telecom Corp)",
                        "$12,850.50 USD",
                        "01/10/2025 - 31/10/2025",
                        "DIAN Colombia"
                ),
                List.of(
                        new AutomationItem("validador_dian.py", AutomationItem.AutomationType.SCRIPT, "Validado DIAN (CUFE OK)", true),
                        new AutomationItem("Webhook SAP ERP (MM)", AutomationItem.AutomationType.WEBHOOK, "Contabilizado", true)
                ),
                new TelemetryData(
                        "Últimos 30 días",
                        22,
                        List.of(3.0, 5.0, 6.0, 10.0, 13.0, 16.0, 19.0, 22.0),
                        "https://app.powerbi.com/groups/me/reports/kore-invoices"
                )
        );
        docs.add(doc4);

        return docs;
    }

    public List<VersionItem> createMockVersions(String docUuid) {
        return List.of(
                new VersionItem("v2.4", "22/10/2026 11:20", "laura.restrepo", "4.2 MB", "Ajuste de cláusula 14.2 sobre limitación de responsabilidad", true),
                new VersionItem("v2.3", "18/09/2026 15:45", "carlos.mendoza", "4.1 MB", "Revisión técnica de métricas de SLA y latencia", false),
                new VersionItem("v2.0", "10/06/2026 09:12", "laura.restrepo", "3.9 MB", "Aprobación formal por Comité de Compras y Riesgos", false),
                new VersionItem("v1.0", "15/01/2026 08:30", "daemon.solr", "3.8 MB", "Carga inicial de plantilla marco estándar", false)
        );
    }

    public List<AuditLogItem> createMockAuditLogs(String docUuid) {
        return List.of(
                new AuditLogItem("22/10/2026 13:42:05", "CHECK_OUT", "laura.restrepo", "192.168.1.45", "Bloqueo exclusivo de nodo para edición de anexos"),
                new AuditLogItem("22/10/2026 13:40:12", "INTEGRITY_OK", "daemon.solr", "10.0.4.12", "Validación exitosa de hash criptográfico SHA-256 e3b0c442"),
                new AuditLogItem("22/10/2026 12:15:30", "OCR_EXTRACT", "daemon.tesseract", "10.0.4.18", "Extracción de entidades contractuales con 99.4% precisión"),
                new AuditLogItem("21/10/2026 17:05:44", "VIEW_DOCUMENT", "juan.perez", "192.168.1.88", "Acceso de lectura en visor PDF integrado"),
                new AuditLogItem("20/10/2026 09:10:19", "TRD_ASSIGNED", "laura.restrepo", "192.168.1.45", "Asignación de serie TRD 210.04 (Contratación)")
        );
    }

    public List<BpmWorkflowStep> createMockBpmSteps(String docUuid) {
        return List.of(
                new BpmWorkflowStep(1, "Radicación & Carga de Expediente", "laura.restrepo", "Completado el 15/01/2026", BpmWorkflowStep.StepState.COMPLETED),
                new BpmWorkflowStep(2, "Validación TRD & Extracción OCR", "daemon.solr", "Completado el 22/10/2026", BpmWorkflowStep.StepState.COMPLETED),
                new BpmWorkflowStep(3, "Revisión Jurídica y Riesgos", "dr.fernando.gomez", "En Proceso (Asignado)", BpmWorkflowStep.StepState.IN_PROGRESS),
                new BpmWorkflowStep(4, "Firma Digital & Custodia WAL", "oficial.cumplimiento", "Pendiente de aprobación previa", BpmWorkflowStep.StepState.PENDING)
        );
    }

    public RepositoryNode createRepositoryTree() {
        RepositoryNode root = new RepositoryNode(
                "Repositorio Raíz (okm:root)",
                "okm:root",
                "okm:node-root-0000",
                "all",
                true
        );

        RepositoryNode contratos = new RepositoryNode(
                "01-Contratos Vigentes",
                "okm:root > Finanzas Corporativas > 01-Contratos Vigentes",
                "okm:node-c001-4fa2-0001",
                "contratos_2025",
                true
        );
        contratos.addChild(new RepositoryNode(
                "Acuerdos Marco 2025",
                "okm:root > Finanzas Corporativas > Contratos y Proveedores 2025",
                "okm:node-7b89-4fa2-91cd",
                "contratos_2025",
                false
        ));
        contratos.addChild(new RepositoryNode(
                "Pólizas de Cumplimiento",
                "okm:root > Finanzas Corporativas > Pólizas de Cumplimiento 2025",
                "okm:node-9c12-3be1-884a",
                "contratos_2025",
                false
        ));
        root.addChild(contratos);

        root.addChild(new RepositoryNode(
                "02-Proveedores Críticos",
                "okm:root > Finanzas Corporativas > 02-Proveedores Críticos",
                "okm:node-11df-55ce-3301",
                "proveedores",
                true
        ));
        root.addChild(new RepositoryNode(
                "03-Bandeja de Aprobaciones",
                "okm:root > Flujos de Trabajo > 03-Bandeja de Aprobaciones",
                "okm:node-bpm0-99aa-7711",
                "all",
                true
        ));
        root.addChild(new RepositoryNode(
                "04-Custodia Legal (Holds)",
                "okm:root > Cumplimiento Legal > 04-Custodia Legal (Holds)",
                "okm:node-44ef-7a91-0021",
                "auditorias",
                true
        ));

        return root;
    }

    public TaxonomyNode createTaxonomyTree() {
        TaxonomyNode root = new TaxonomyNode(
                "/okm:root",
                "okm:root",
                "okm:taxo-root-0000",
                "all"
        );

        TaxonomyNode finanzas = new TaxonomyNode(
                "01-Finanzas",
                "okm:root > Finanzas Corporativas",
                "okm:taxo-fin0-1111",
                "contratos_2025"
        );

        TaxonomyNode contratos2025 = new TaxonomyNode(
                "Contratos_2025",
                "okm:root > Finanzas Corporativas > Contratos y Proveedores 2025",
                "okm:node-7b89-4fa2-91cd",
                "contratos_2025",
                18
        );
        contratos2025.addChild(new TaxonomyNode(
                "Presupuestos_CapEx",
                "okm:root > Finanzas > Presupuestos CapEx",
                "okm:taxo-cpx0-2222",
                "contratos_2025"
        ));
        contratos2025.addChild(new TaxonomyNode(
                "Facturación_Proveedores",
                "okm:root > Finanzas > Facturación Proveedores",
                "okm:node-11df-55ce-3301",
                "proveedores"
        ));
        finanzas.addChild(contratos2025);

        root.addChild(finanzas);
        root.addChild(new TaxonomyNode(
                "02-RRHH / Expedientes",
                "okm:root > Recursos Humanos",
                "okm:taxo-rrhh-4444",
                "rrhh"
        ));
        root.addChild(new TaxonomyNode(
                "03-Legal / Litigios",
                "okm:root > Dirección Jurídica > Litigios",
                "okm:taxo-leg0-5555",
                "legal"
        ));
        root.addChild(new TaxonomyNode(
                "04-Auditorías Externas",
                "okm:root > Cumplimiento > 04-Auditorías Externas",
                "okm:node-44ef-7a91-0021",
                "auditorias"
        ));

        return root;
    }

    public UserProfile createUserProfile() {
        return new UserProfile("laura.restrepo", "Legal Ops Senior", "LR");
    }

    public LegalContext createLegalContext() {
        return new LegalContext(
                "CC-4020",
                "Dirección Jurídica & Cumplimiento",
                "Laura Restrepo",
                "Corporativa Bogotá - Piso 9"
        );
    }

    public RepositoryQuota createRepositoryQuota() {
        return new RepositoryQuota(24.5, 245, 1024);
    }
}
