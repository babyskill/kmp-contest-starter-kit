import os
from PIL import Image, ImageDraw, ImageFont

def create_held_out_label(output_path="docs/qa-v2/held_out_label_almond_butter.png"):
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    
    # 800 x 1100 px clean label
    width, height = 800, 1100
    img = Image.new("RGB", (width, height), color=(255, 255, 255))
    draw = ImageDraw.Draw(img)

    # Use system TTF or default
    try:
        font_title = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 36)
        font_header = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 28)
        font_bold = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 22)
        font_regular = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 19)
        font_small = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 16)
    except Exception:
        font_title = font_header = font_bold = font_regular = font_small = ImageFont.load_default()

    # Outer border
    draw.rectangle([20, 20, width - 20, height - 20], outline=(0, 0, 0), width=4)

    # Header
    draw.text((40, 40), "NATURE'S HARVEST", fill=(40, 80, 40), font=font_header)
    draw.text((40, 80), "ALMOND BUTTER — CREAMY", fill=(0, 0, 0), font=font_title)
    draw.text((40, 130), "All Natural • No Added Sugar • Non-GMO", fill=(80, 80, 80), font=font_regular)

    draw.line([(40, 165), (width - 40, 165)], fill=(0, 0, 0), width=6)

    # Nutrition Facts Title
    draw.text((40, 175), "Nutrition Facts", fill=(0, 0, 0), font=font_header)
    draw.text((40, 215), "14 servings per container", fill=(0, 0, 0), font=font_regular)
    draw.text((40, 240), "Serving size                 2 Tbsp (32g)", fill=(0, 0, 0), font=font_bold)

    draw.line([(40, 275), (width - 40, 275)], fill=(0, 0, 0), width=10)

    # Calories section
    draw.text((40, 295), "Amount per serving", fill=(0, 0, 0), font=font_small)
    draw.text((40, 320), "Calories", fill=(0, 0, 0), font=font_header)
    draw.text((width - 160, 310), "190", fill=(0, 0, 0), font=font_title)

    draw.line([(40, 365), (width - 40, 365)], fill=(0, 0, 0), width=4)

    draw.text((width - 150, 375), "% Daily Value*", fill=(0, 0, 0), font=font_small)
    draw.line([(40, 400), (width - 40, 400)], fill=(0, 0, 0), width=1)

    # Nutrition lines
    y = 410
    lines = [
        ("Total Fat 17g", "22%", True),
        ("  Saturated Fat 1.5g", "8%", False),
        ("  Trans Fat 0g", "", False),
        ("Cholesterol 0mg", "0%", True),
        ("Sodium 65mg", "3%", True),
        ("Total Carbohydrate 7g", "3%", True),
        ("  Dietary Fiber 4g", "14%", False),
        ("  Total Sugars 2g", "", False),
        ("    Includes 0g Added Sugars", "0%", False),
        ("Protein 7g", "14%", True),
    ]

    for label, dv, is_bold in lines:
        f = font_bold if is_bold else font_regular
        draw.text((40, y), label, fill=(0, 0, 0), font=f)
        if dv:
            draw.text((width - 100, y), dv, fill=(0, 0, 0), font=font_bold)
        y += 28
        draw.line([(40, y - 2), (width - 40, y - 2)], fill=(180, 180, 180), width=1)

    draw.line([(40, y), (width - 40, y)], fill=(0, 0, 0), width=6)
    y += 15

    # Vitamins
    draw.text((40, y), "Vitamin D 0mcg 0%  •  Calcium 70mg 6%", fill=(60, 60, 60), font=font_small)
    y += 24
    draw.text((40, y), "Iron 1.1mg 6%  •  Potassium 240mg 6%", fill=(60, 60, 60), font=font_small)
    y += 35

    draw.line([(40, y), (width - 40, y)], fill=(0, 0, 0), width=4)
    y += 20

    # Ingredients & Allergens
    draw.text((40, y), "INGREDIENTS:", fill=(0, 0, 0), font=font_bold)
    y += 30
    draw.text((40, y), "Roasted Almonds, Sea Salt.", fill=(0, 0, 0), font=font_header)
    y += 45

    draw.text((40, y), "CONTAINS: TREE NUTS (ALMONDS).", fill=(180, 0, 0), font=font_bold)
    y += 30
    draw.text((40, y), "MANUFACTURED IN A FACILITY THAT ALSO PROCESSES PEANUTS,", fill=(60, 60, 60), font=font_small)
    y += 22
    draw.text((40, y), "SOY, AND OTHER TREE NUTS.", fill=(60, 60, 60), font=font_small)
    y += 40

    # Barcode representation
    draw.line([(40, y), (width - 40, y)], fill=(200, 200, 200), width=1)
    y += 20
    draw.text((40, y), "DISTRIBUTED BY: NATURE'S HARVEST FOODS, BERKELEY, CA 94710", fill=(100, 100, 100), font=font_small)

    img.save(output_path)
    print(f"Generated realistic held-out label image at: {output_path}")

if __name__ == "__main__":
    create_held_out_label()
