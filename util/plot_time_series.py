"""
Utility for plotting C2C time series data for debugging.

Steps to use:
1. Run the C2C commandline as follows:
  a. Run without interpolation and --printInput=true and redirect stderr:
    ```
    ./bazel-bin/c2c \
    --printInput=true --includeRegrowth=true \
    --inputFile=../javatests/it/unibo/c2c/testdata/input.csv
    1>/tmp/output-no-interp.csv 2>/tmp/input-with-infill.csv
    ```
  b. Run with interpolation and --printInput=true and redirect stderr:
    ```
    ./bazel-bin/c2c \
    --printInput=true --interpolateRegrowth=true --includeRegrowth=true \
    --inputFile=../javatests/it/unibo/c2c/testdata/input.csv
    1>/tmp/output-interp.csv 2>/tmp/input-interp.csv
    ```
2. Run the plot script and include the time series ID (starting from 0) to plot:
  ```
  python3 plot_time_series.py \
    --input_csv=/tmp/input-with-infill.csv \
    --interped_csv=/tmp/input-interp.csv \
    --output_csv=/tmp/output-interp.csv \
    0
  ```
"""

import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.dates as mdates
import argparse
from matplotlib.table import Table
import numpy as np

def plot_time_series(input_csv, interped_csv, output_csv, time_series_index):
    try:
        # Read the CSV files
        input_df = pd.read_csv(input_csv)
        interped_df = pd.read_csv(interped_csv)
        output_df = pd.read_csv(output_csv)

        # Validate index
        if time_series_index < 0 or time_series_index >= len(input_df):
            print(f"Error: Index {time_series_index} is out of bounds.")
            return

        # Get the ID for the selected index
        time_series_id = input_df.iloc[time_series_index, 0]
        # Filter output data for the selected ID
        breakpoints = output_df[output_df['id'] == time_series_index ]
        print(breakpoints)
        # Extract time series data
        input_ts = input_df.iloc[time_series_index, 1:].T
        interped_ts = interped_df.iloc[time_series_index, 1:].T

        input_ts.index = pd.to_datetime(input_ts.index, format='%Y.0')
        interped_ts.index = pd.to_datetime(interped_ts.index, format='%Y.0')

        # Replace 0 with NaN for better plotting
        input_ts = input_ts.replace(0, np.nan)

        # Create the plot
        fig, axes = plt.subplots(2, 1, figsize=(18, 12), gridspec_kw={'height_ratios': [2, 1]})
        ax1 = axes[0]
        ax_table = axes[1]

        # Subplot 1: Original vs Interpolated Time Series
        ax1.plot(input_ts.index, input_ts.values, marker='o', linestyle='None', label='Original', color='blue', alpha=0.6)
        ax1.plot(interped_ts.index, interped_ts.values, marker='.', linestyle='--', label='Interpolated', color='green', alpha=0.8)

        # Add breakpoint vertical lines
        added_labels = set()
        if not breakpoints.empty:
            for _, bp in breakpoints.iterrows():
                try:
                    bp_year = pd.to_datetime(str(int(bp['year'])), format='%Y')
                    if bp_year not in added_labels:
                        ax1.axvline(x=bp_year, color='red', linestyle='--', alpha=0.7, label=f'Breakpoint {bp["year"]}')
                        added_labels.add(bp_year)
                    else:
                        ax1.axvline(x=bp_year, color='red', linestyle='--', alpha=0.7)
                except ValueError:
                    print(f"Warning: Could not parse year {bp['year']} for breakpoint.")

        ax1.set_title(f'Time Series Analysis for ID: {time_series_id} (Index: {time_series_index})')
        ax1.set_xlabel('Year')
        ax1.set_ylabel('Value')
        ax1.grid(True)
        ax1.xaxis.set_major_formatter(mdates.DateFormatter('%Y'))
        handles, labels = ax1.get_legend_handles_labels()
        by_label = dict(zip(labels, handles))
        ax1.legend(by_label.values(), by_label.keys(), loc='upper left')

        # Subplot 2: Table of Regrowth Metrics
        ax_table.axis('off')
        ax_table.axis('tight')

        if not breakpoints.empty:
            table_data = breakpoints[['year', 'magnitude', 'duration', 'rate', 'recoveryIndicator', 'regrowth60', 'regrowth80', 'regrowth100']].copy()
            table_data.columns = ['Year', 'Mag', 'Dur', 'Rate', 'Rec Ind.', 'Reg 60%', 'Reg 80%', 'Reg 100%']
            table_data = table_data.round(2) # Round for better display

            tbl = Table(ax_table, bbox=[0, 0, 1, 1])

            nrows, ncols = table_data.shape
            width, height = 1.0 / ncols, 1.0 / (nrows + 1)

            # Add headers
            for j, col in enumerate(table_data.columns):
                tbl.add_cell(0, j, width, height, text=col, loc='center', facecolor='lightgray')

            # Add data
            for i in range(nrows):
                for j, val in enumerate(table_data.iloc[i]):
                    tbl.add_cell(i + 1, j, width, height, text=val, loc='center')

            tbl.auto_set_font_size(False)
            tbl.set_fontsize(8)
            ax_table.add_table(tbl)
            ax_table.set_title('Breakpoint and Regrowth Metrics', pad=10)
        else:
            ax_table.text(0.5, 0.5, 'No breakpoint data found for this ID.', horizontalalignment='center', verticalalignment='center')

        plt.tight_layout()
        plt.show()

    except FileNotFoundError:
        print("Error: One or more CSV files not found.")
    except Exception as e:
        print(f"An error occurred: {e}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Plot time series data with breakpoints.')
    parser.add_argument('index', type=int, help='Zero-based index of the time series to plot.')
    parser.add_argument('--input_csv', type=str, default='src/C2C-GEE/javatests/it/unibo/c2c/testdata/input.csv', help='Path to the input CSV file.')
    parser.add_argument('--interped_csv', type=str, default='src/C2C-GEE/javatests/it/unibo/c2c/testdata/interped-input-negonly.csv', help='Path to the interpolated input CSV file.')
    parser.add_argument('--output_csv', type=str, default='src/C2C-GEE/javatests/it/unibo/c2c/testdata/output-regrowth-negonly.csv', help='Path to the output/breakpoints CSV file.')

    args = parser.parse_args()

    plot_time_series(args.input_csv, args.interped_csv, args.output_csv, args.index)
