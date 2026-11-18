import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_BREAK
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import nsdecls, qn
import os

def update_document():
    doc_path = 'UCS553 Enterprise Web Application.docx'
    doc = docx.Document(doc_path)
    
    print(f"Loaded {doc_path} with {len(doc.paragraphs)} paragraphs and {len(doc.tables)} tables.")

    # 1. Update Cover Page: Insert Thapar Logo in P11
    p11 = doc.paragraphs[11]
    p11.text = ""
    p11.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run_logo = p11.add_run()
    if os.path.exists('extracted_images/thapar_logo.jpeg'):
        run_logo.add_picture('extracted_images/thapar_logo.jpeg', width=Inches(1.8))
        print("Inserted Thapar logo at P11.")

    # 2. Add Caption Helper
    def format_caption(p, text):
        p.text = text
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        for run in p.runs:
            run.font.size = Pt(9.5)
            run.font.italic = True
            run.font.bold = True
            run.font.color.rgb = RGBColor(71, 85, 105) # Slate 600

    # 3. Add Figure Captions for Chapter 2 UML Diagrams
    # P107 has System Architecture
    format_caption(doc.paragraphs[108], "Figure 2.1: TenderX High-Level N-Tier System Architecture")
    # P115 has Use Case Diagram
    format_caption(doc.paragraphs[116], "Figure 2.2: Use Case Diagram for Tender Issuer, Contractor, and Auditor")
    # P120 has Class Diagram
    format_caption(doc.paragraphs[121], "Figure 2.3: Core Domain Model Class Diagram")
    # P125 has Activity Diagram
    format_caption(doc.paragraphs[126], "Figure 2.4: Procurement Lifecycle Activity Diagram")
    # P130 has ER Diagram
    format_caption(doc.paragraphs[131], "Figure 2.5: Entity-Relationship (ER) Schema Diagram with Dynamic Attributes")

    # 4. Fix placeholders and replace \\newpage with Page Breaks
    for i, p in enumerate(doc.paragraphs):
        # Fix \newpage
        if p.text.strip() == '\\newpage':
            p.text = ""
            r = p.add_run()
            r.add_break(WD_BREAK.PAGE)
            continue

        # Fix missing math placeholders
        if 'calibrates weights (), and officially seals' in p.text:
            p.text = p.text.replace('calibrates weights (), and officially seals', 'calibrates weights (Total = 100%), and officially seals')
        
        if 'using standardized inverse-cost ratio formulas () and engineering' in p.text:
            p.text = p.text.replace('using standardized inverse-cost ratio formulas () and engineering', 'using standardized inverse-cost ratio formulas ((L1 / Price) * Weight) and engineering')

        if 'public procurement inverse-cost formula () and structured' in p.text:
            p.text = p.text.replace('public procurement inverse-cost formula () and structured', 'public procurement inverse-cost formula ((L1 / Price) * Weight) and structured')

        if 'lowest valid bid ():' in p.text:
            p.text = p.text.replace('lowest valid bid ():', 'lowest valid bid ((L1 / Price) * Weight):')

        if 'reviews weights (), adds custom' in p.text:
            p.text = p.text.replace('reviews weights (), adds custom', 'reviews weights (Total = 100%), adds custom')

        if 'weights totaling , the validation' in p.text:
            p.text = p.text.replace('weights totaling , the validation', 'weights totaling 105%, the validation')

        if 'Submissions exceeding  of the estimated' in p.text:
            p.text = p.text.replace('Submissions exceeding  of the estimated', 'Submissions exceeding 115% of the estimated')

        if 'Issuer (Publisher) logs in  navigates to AI Designer' in p.text:
            p.text = p.text.replace('Issuer (Publisher) logs in  navigates to AI Designer  inputs:', 'Issuer (Publisher) logs in → navigates to AI Designer → inputs:')

        if 'Contractors (Bidders) log in  complete the dynamic fields  submit sealed proposals.' in p.text:
            p.text = p.text.replace('Contractors (Bidders) log in  complete the dynamic fields  submit sealed proposals.', 'Contractors (Bidders) log in → complete dynamic fields → submit sealed proposals.')

        if 'Auditor opens the Evaluation Room  executes the deterministic evaluation  reviews' in p.text:
            p.text = p.text.replace('Auditor opens the Evaluation Room  executes the deterministic evaluation  reviews', 'Auditor opens the Evaluation Room → executes deterministic evaluation → reviews')

    # 5. Insert Screenshots in Chapter 4
    # P206 is empty between 4.1 description and 4.2
    p206 = doc.paragraphs[206]
    p206.text = ""
    p206.alignment = WD_ALIGN_PARAGRAPH.CENTER
    if os.path.exists('extracted_images/screen_dashboard.png'):
        r = p206.add_run()
        r.add_picture('extracted_images/screen_dashboard.png', width=Inches(5.8))
        # Add caption below picture
        p_cap = doc.add_paragraph()
        format_caption(p_cap, "Figure 4.1: TenderX Operational Dashboard & Live Procurement Metrics")
        # Move caption right after p206
        p206._p.addnext(p_cap._p)
        print("Inserted Figure 4.1 Dashboard screenshot.")

    # P213 is empty between 4.2 walkthrough and 4.3 Case Study
    p213 = doc.paragraphs[213]
    p213.text = ""
    p213.alignment = WD_ALIGN_PARAGRAPH.CENTER
    
    # We will insert Figure 4.2 Login and Figure 4.3 Designer
    curr_elem = p213._p
    if os.path.exists('extracted_images/screen_login.png'):
        p_login = doc.add_paragraph()
        p_login.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_login.add_run().add_picture('extracted_images/screen_login.png', width=Inches(5.4))
        p_cap_login = doc.add_paragraph()
        format_caption(p_cap_login, "Figure 4.2: Multi-Role Authentication Portal with 3 Preconfigured Bidders & Custom Registration")
        curr_elem.addnext(p_login._p)
        p_login._p.addnext(p_cap_login._p)
        curr_elem = p_cap_login._p
        print("Inserted Figure 4.2 Login screenshot.")

    if os.path.exists('extracted_images/screen_designer.png'):
        p_des = doc.add_paragraph()
        p_des.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_des.add_run().add_picture('extracted_images/screen_designer.png', width=Inches(5.8))
        p_cap_des = doc.add_paragraph()
        format_caption(p_cap_des, "Figure 4.3: AI Tender Specification Designer with Real-Time Weight Tuning & Dynamic Field Builder")
        curr_elem.addnext(p_des._p)
        p_des._p.addnext(p_cap_des._p)
        curr_elem = p_cap_des._p
        print("Inserted Figure 4.3 Designer screenshot.")

    # P219 is before Comparative Evaluation Results Table
    p219 = doc.paragraphs[219]
    p219.text = ""
    p219.alignment = WD_ALIGN_PARAGRAPH.CENTER
    curr_elem_case = p219._p
    
    if os.path.exists('extracted_images/screen_bidder.png'):
        p_bid = doc.add_paragraph()
        p_bid.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_bid.add_run().add_picture('extracted_images/screen_bidder.png', width=Inches(5.4))
        p_cap_bid = doc.add_paragraph()
        format_caption(p_cap_bid, "Figure 4.4: Dynamic Contractor Bid Submission Form Generated from AI Schema")
        curr_elem_case.addnext(p_bid._p)
        p_bid._p.addnext(p_cap_bid._p)
        curr_elem_case = p_cap_bid._p
        print("Inserted Figure 4.4 Bidder screenshot.")

    # P225 is after Table 1
    p225 = doc.paragraphs[225]
    p225.text = ""
    p225.alignment = WD_ALIGN_PARAGRAPH.CENTER
    if os.path.exists('extracted_images/screen_eval.png'):
        p_eval = doc.add_paragraph()
        p_eval.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_eval.add_run().add_picture('extracted_images/screen_eval.png', width=Inches(5.8))
        p_cap_eval = doc.add_paragraph()
        format_caption(p_cap_eval, "Figure 4.5: Deterministic Evaluation Leaderboard, Winner Award Card & Formula Audit Traces")
        p225._p.addnext(p_eval._p)
        p_eval._p.addnext(p_cap_eval._p)
        print("Inserted Figure 4.5 Evaluation screenshot.")

    # 6. Style Tables nicely
    for tbl in doc.tables:
        tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
        # Format header row
        hdr_cells = tbl.rows[0].cells
        for cell in hdr_cells:
            # Set background shading
            shading = parse_xml(r'<w:shd {} w:fill="1E293B"/>'.format(nsdecls('w')))
            cell._tc.get_or_add_tcPr().append(shading)
            for p in cell.paragraphs:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                for run in p.runs:
                    run.font.bold = True
                    run.font.color.rgb = RGBColor(255, 255, 255)
                    run.font.size = Pt(8.5)

        # Style data rows
        for r_idx in range(1, len(tbl.rows)):
            row = tbl.rows[r_idx]
            is_odd = (r_idx % 2 == 1)
            bg_color = "F8FAFC" if is_odd else "FFFFFF"
            for c_idx, cell in enumerate(row.cells):
                shading = parse_xml(r'<w:shd {} w:fill="{}"/>'.format(nsdecls('w'), bg_color))
                cell._tc.get_or_add_tcPr().append(shading)
                for p in cell.paragraphs:
                    if c_idx == 0:
                        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                    for run in p.runs:
                        run.font.size = Pt(8)

    # 7. Format Code Snippets (P190, P193)
    for idx in [190, 193]:
        if idx < len(doc.paragraphs):
            p = doc.paragraphs[idx]
            for run in p.runs:
                run.font.name = 'Consolas'
                run.font.size = Pt(8)
                run.font.color.rgb = RGBColor(30, 41, 59)

    doc.save('UCS553 Enterprise Web Application.docx')
    print("Successfully updated UCS553 Enterprise Web Application.docx!")

if __name__ == '__main__':
    update_document()

