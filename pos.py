import os
import csv
import re
import datetime
import time
from decimal import Decimal


class POSSystem:
    def __init__(self):
        self.basket = []
        self.data = "data"
        self.bills = os.path.join(self.data, "bills")
        self.tax = os.path.join(self.data, "tax")
        self.directories()

    def directories(self):
        """Creating directories if they don't exist"""
        try:
            if not os.path.exists(self.data):
                os.makedirs(self.data)
            if not os.path.exists(self.bills):
                os.makedirs(self.bills)
            if not os.path.exists(self.tax):
                os.makedirs(self.tax)
        except Exception as e:
            print(f"Error occured: {e}")

    def validate_item_code(self, item_code):
        """Validating the item code"""
        pattern = r'^[a-zA-Z0-9_]+$'
        return bool(re.match(pattern, item_code))

    def add_to_basket(self):
        """Add items to basket"""
        continue_adding = True

        while continue_adding:
            print("\n--- Add Item to Basket ---\n")

            # Validate item code
            while True:
                item_code = input("Enter item code \n(item code can only contain letters, numbers, underscores): ")
                if self.validate_item_code(item_code):
                    break
                print("Error: Invalid item code. \nUse only letters, numbers, and underscores.")

            #pricing
            try:
                internal_price = Decimal(input("Enter internal price: $ "))
                if internal_price < 0:
                    raise ValueError("Price cannot be negative.")

                discount = Decimal(input("Enter discount amount: $ "))
                if discount < 0:
                    raise ValueError("Discount cannot be negative.")

                sale_price = Decimal(input("Enter sale price: $ "))
                if sale_price < 0:
                    raise ValueError("Price cannot be negative.")

                quantity = int(input("Enter quantity: "))
                if quantity <= 0 or not isinstance(quantity, int):
                    raise ValueError("Quantity must be an integer and greater than zero.")


                #line total
                line_total = (sale_price - discount) * quantity

                #Adding to basket
                self.basket.append({
                    'item_code': item_code,
                    'internal_price': internal_price,
                    'discount': discount,
                    'sale_price': sale_price,
                    'quantity': quantity,
                    'line_total': line_total
                })

                #Displaying  basket
                self.display_basket()

                #Ask to continue adding items
                choice = input("\nAdd more items? (1 - Add more , 0 - Done): ")
                if choice == '0':
                    continue_adding = False
                elif choice != '1':
                    print("Invalid option. Returning to menu by default..")
                    time.sleep(2)
                    continue_adding = False

            except ValueError as error:
                print(f"Invalid input: {error}")

    def display_basket(self):
        """Display items in the basket (with line numbers)"""
        if not self.basket:
            print("\nBasket is empty.")
            return

        print("\n--- Current Basket ---")
        print("Line | Item Code | Int. Price | Discount | Sale Price | Quantity | Line Total")
        print("-" * 75)

        for idx, item in enumerate(self.basket, 1):
            print(f"{idx:4} | {item['item_code']:9} | {item['internal_price']:10}$ | {item['discount']:8}$ | "
                  f"{item['sale_price']:10}$ | {item['quantity']:8} | {item['line_total']:10}$")

    def delete_item(self):
        """Delete an item from the basket using the line numbers"""
        if not self.basket:
            print("\nBasket is empty. Nothing to delete.")
            return

        self.display_basket()

        try:
            line_num = int(input("\nEnter line number to delete: "))
            if 1 <= line_num <= len(self.basket):
                deleted_item = self.basket.pop(line_num - 1)
                print(f"Deleted item: {deleted_item['item_code']}")
                self.display_basket()
            else:
                print("Invalid line number.")
        except ValueError:
            print("Please enter a valid number.")

    def update_item(self):
        """Update an item using line number"""
        if not self.basket:
            print("\nBasket is empty. Nothing to update.")
            return
        self.display_basket()

        try:
            line_num = int(input("\nEnter line number to update: "))
            if 1 <= line_num <= len(self.basket):
                item = self.basket[line_num - 1]
                print(f"\nUpdating item: {item['item_code']}")

                # Get new values
                try:
                    sale_price = Decimal(input(f"Enter new sale price [{item['sale_price']}]: ") or item['sale_price'])
                    if sale_price < 0:
                        raise ValueError("Price cannot be negative.")

                    discount = Decimal(input(f"Enter new discount [{item['discount']}]: ") or item['discount'])
                    if discount < 0:
                        raise ValueError("Discount cannot be negative.")

                    quantity = int(input(f"Enter new quantity [{item['quantity']}]: ") or item['quantity'])
                    if quantity <= 0 or not isinstance(quantity, int):
                        raise ValueError("Quantity must be an integer and greater than zero.")

                    # Update item
                    item['sale_price'] = sale_price
                    item['discount'] = discount
                    item['quantity'] = quantity
                    item['line_total'] = (sale_price - discount) * quantity

                    print("Item updated successfully.")
                    self.display_basket()

                except ValueError as e:
                    print(f"Invalid input: {e}")
            else:
                print("Invalid line number.")
        except ValueError:
            print("Please enter a valid number.")

    def generate_bill_number(self):
        """Generate a bill number"""
        today = datetime.date.today().strftime("%Y%m%d")

        # Find the latest bill number for today
        pattern = f"{today}_"
        latest_num = 0

        try:
            for filename in os.listdir(self.bills):
                if filename.startswith(pattern) and filename.endswith(".csv"):
                    try:
                        num = int(filename[len(pattern):-4])
                        latest_num = max(latest_num, num)
                    except ValueError:
                        pass
        except Exception as e:
            print(f"Error reading bills directory: {e}")

        # Create new bill number
        new_num = latest_num + 1
        return f"{today}_{new_num:04d}"

    def generate_bill(self):
        """Generate and save bill, calculate grand total"""
        if not self.basket:
            print("\nBasket is empty. Cannot generate bill.")
            return

        bill_number = self.generate_bill_number()
        grand_total = sum(item['line_total'] for item in self.basket)

        # Display bill
        print("\n--- Bill Generated ---")
        print(f"Bill Number: {bill_number}")
        self.display_basket()
        print(f"\nGrand Total: {grand_total}")

        # Save bill to CSV
        try:
            bill_file = os.path.join(self.bills, f"{bill_number}.csv")
            with open(bill_file, 'w', newline='') as csvfile:
                writer = csv.writer(csvfile)
                writer.writerow(['Bill Number', bill_number])
                writer.writerow(['Date', datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")])
                writer.writerow([])
                writer.writerow(['Item Code', 'Internal Price', 'Discount', 'Sale Price', 'Quantity', 'Line Total'])

                for item in self.basket:
                    writer.writerow([
                        item['item_code'],
                        item['internal_price'],
                        item['discount'],
                        item['sale_price'],
                        item['quantity'],
                        item['line_total']
                    ])

                writer.writerow([])
                writer.writerow(['Grand Total', grand_total])

            print(f"Bill saved as {bill_file}")

            # Clear basket
            self.basket = []

        except Exception as e:
            print(f"Error saving bill: {e}")

    def search_bill(self):
        """Search and display a bill by bill number"""
        bill_number = input("Enter bill number to search: ")
        bill_file = os.path.join(self.bills, f"{bill_number}.csv")

        if os.path.exists(bill_file):
            try:
                print(f"\n--- Bill #{bill_number} ---")
                with open(bill_file, 'r', newline='') as csvfile:
                    reader = csv.reader(csvfile)
                    for row in reader:
                        if row:  # Skip empty rows
                            print(" | ".join(str(cell) for cell in row))
            except Exception as e:
                print(f"Error reading bill: {e}")
        else:
            print(f"Bill #{bill_number} not found.")

    def calculate_checksum(self, transaction_line):
        """Calculate checksum for a transaction line"""
        uppercase_count = sum(1 for char in transaction_line if char.isupper())
        lowercase_count = sum(1 for char in transaction_line if char.islower())
        numbers_count = sum(1 for char in transaction_line if char.isdigit() or char == '.')

        return uppercase_count + lowercase_count + numbers_count

    def generate_tax_file(self):
        """Generate tax transaction file with checksums"""
        if not os.listdir(self.bills):
            print("\nNo bills found. Cannot generate tax file.")
            return

        try:
            # filename
            timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
            tax_file = os.path.join(self.tax, f"tax_{timestamp}.csv")

            with open(tax_file, 'w', newline='') as csvfile:
                writer = csv.writer(csvfile)
                writer.writerow(['Bill Number', 'Item Code', 'Sale Price', 'Quantity', 'Line Total', 'Checksum'])

                for bill_filename in os.listdir(self.bills):
                    if bill_filename.endswith(".csv"):
                        bill_number = bill_filename[:-4]  # Remove .csv extension
                        bill_file = os.path.join(self.bills, bill_filename)

                        with open(bill_file, 'r', newline='') as bill_csv:
                            reader = csv.reader(bill_csv)
                            in_items_section = False

                            for row in reader:
                                if not row:
                                    continue

                                if row[0] == 'Item Code':
                                    in_items_section = True
                                    continue

                                if in_items_section and row[0] != 'Grand Total':
                                    #item row
                                    item_code = row[0]
                                    sale_price = row[3]
                                    quantity = row[4]
                                    line_total = row[5]

                                    # Create transaction line
                                    transaction = f"{bill_number},{item_code},{sale_price},{quantity},{line_total}"
                                    checksum = self.calculate_checksum(transaction)

                                    # Write transaction with checksum
                                    writer.writerow(
                                        [bill_number, item_code, sale_price, quantity, line_total, checksum])

            print(f"Tax file generated: {tax_file}")

        except Exception as e:
            print(f"Error generating tax file: {e}")

    def main_menu(self):
        """Displaying main menu & handling user choices"""
        while True:
            print("\n===== Cupcake POS System =====\n")
            print("1. Add Items to the Basket")
            print("2. Delete Item from Basket")
            print("3. Update Item")
            print("4. Generate Bill")
            print("5. Search Bill")
            print("6. Generate Tax Transaction File")
            print("0. Exit")
            print("\n=================================")

            choice = input("\nEnter your choice: ")

            try:
                match choice:
                    case '1':
                        self.add_to_basket()
                    case '2':
                        self.delete_item()
                    case '3':
                        self.update_item()
                    case '4':
                        self.generate_bill()
                    case '5':
                        self.search_bill()
                    case '6':
                        self.generate_tax_file()
                    case '0':
                        print("Exiting system..!")
                        break
                    case _:
                        print("Error: Invalid choice! (0-6)")
            except Exception as e:
                print(f"Error: {e}")
