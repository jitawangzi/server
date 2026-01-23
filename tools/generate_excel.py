import pandas as pd
import sys
import json
import os

def generate_excel(json_path):
    try:
        with open(json_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        file_name = data.get('fileName')
        if not file_name:
            print("Error: 'fileName' is required in the JSON input.")
            return

        # Ensure the filename ends with .xlsx
        if not file_name.endswith('.xlsx'):
            file_name += '.xlsx'
            
        output_path = os.path.join(os.path.dirname(json_path), file_name)
        
        with pd.ExcelWriter(output_path, engine='openpyxl') as writer:
            for sheet in data.get('sheets', []):
                sheet_name = sheet.get('name')
                # 4 rows structure: CS, FieldName, Type, Comment
                header_data = [
                    sheet.get('cs', []),      # Row 1: c/s/cs
                    sheet.get('fields', []),  # Row 2: Field Names
                    sheet.get('types', []),   # Row 3: Data Types
                    sheet.get('comments', []) # Row 4: Comments
                ]
                
                # Check for consistency
                col_count = len(sheet.get('fields', []))
                for i, row in enumerate(header_data):
                    if len(row) != col_count:
                        # Fill missing with empty strings or truncate
                        if len(row) < col_count:
                            header_data[i] = row + [''] * (col_count - len(row))
                        else:
                            header_data[i] = row[:col_count]

                df = pd.DataFrame(header_data)
                # Write to sheet, no index, no header (since we constructed it manually)
                df.to_excel(writer, sheet_name=sheet_name, index=False, header=False)
                
        print(f"Successfully generated {output_path}")

    except Exception as e:
        print(f"Error generating Excel: {str(e)}")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python generate_excel.py <json_config_path>")
    else:
        generate_excel(sys.argv[1])
