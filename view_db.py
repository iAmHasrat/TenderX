import sqlite3
import sys

def view_database():
    db_file = "tenderx.db"
    conn = sqlite3.connect(db_file)
    cursor = conn.cursor()

    print("=" * 70)
    print("               TenderX SQLite Database Inspector")
    print(f"               File: {db_file}")
    print("=" * 70)

    # 1. Tenders Table
    print("\n[1] TABLE: tenders")
    print("-" * 70)
    cursor.execute("SELECT id, title, category, estimated_budget, max_timeline_days, status, awarded_bidder_name FROM tenders")
    rows = cursor.fetchall()
    for r in rows:
        print(f"• ID: {r[0]}")
        print(f"  Title:    {r[1]}")
        print(f"  Category: {r[2]} | Budget: ₹{r[3]:,.0f} | Deadline: {r[4]} days")
        print(f"  Status:   {r[5]} | Awarded To: {r[6] or 'None (Open)'}")
        print()

    # 2. Criteria & Fields Summary
    print("\n[2] TABLE: tender_criteria (Sample breakdown for first tender)")
    print("-" * 70)
    cursor.execute("SELECT id, tender_id, criterion_key, name, weight FROM tender_criteria LIMIT 5")
    crit_rows = cursor.fetchall()
    for c in crit_rows:
        print(f"  [{c[2]}] {c[3]} (Weight: {c[4]}%)")
        # Get fields under this criterion
        cursor.execute("SELECT field_key, label, field_type, is_required FROM criterion_fields WHERE criterion_id = ?", (c[0],))
        fields = cursor.fetchall()
        for f in fields:
            print(f"     └─ Field: {f[0]} ({f[2]}) -> \"{f[1]}\" [Required: {'Yes' if f[3] else 'No'}]")
    print()

    # 3. Bids Table
    print("\n[3] TABLE: bids")
    print("-" * 70)
    cursor.execute("SELECT id, tender_id, bidder_name, total_quote, proposed_timeline_days, status, total_score FROM bids")
    bids = cursor.fetchall()
    for b in bids:
        print(f"• Bid ID: {b[0]} | Target: {b[1]}")
        print(f"  Contractor:  {b[2]}")
        print(f"  Quote:       ₹{b[3]:,.0f} | Proposed Days: {b[4]} days")
        print(f"  Evaluation:  Score: {b[6]}/100 | Status: {b[5]}")
        print()

    # 4. Bid Responses Sample
    print("\n[4] TABLE: bid_responses (Sample Dynamic Field Values for BID-101)")
    print("-" * 70)
    cursor.execute("SELECT field_key, field_label, response_value FROM bid_responses WHERE bid_id = 'BID-101' LIMIT 4")
    resps = cursor.fetchall()
    for resp in resps:
        val = resp[2]
        if len(val) > 60:
            val = val[:57] + "..."
        print(f"  • {resp[0]}: \"{val}\"")

    print("\n" + "=" * 70)
    print("Database connection closed cleanly.")
    print("=" * 70)
    conn.close()

if __name__ == "__main__":
    view_database()

