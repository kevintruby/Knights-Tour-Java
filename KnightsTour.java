import java.util.*;

public class KnightsTour {
	private static int gridSize = 8;
	private static int attempt_count = 0;
	private static int[] starting_coordinates;
	private static Boolean is_closed_tour = true;

	public enum DirectionDeltas {
		NNW(new int[] { -2, -1 }), NNE(new int[] { -2,  1 }),
		ENE(new int[] { -1,  2 }), ESE(new int[] {  1,  2 }),
		SSE(new int[] {  2,  1 }), SSW(new int[] {  2, -1 }),
		WSW(new int[] {  1, -2 }), WNW(new int[] { -1, -2 })
		;
		private final int[] delta;
		DirectionDeltas(int[] delta) {
			this.delta = delta;
		}
		public int[] getDeltas() {
			return this.delta;
		}
	}

	private static int[] findLastMove(int[][] matrix) {
		int progress_count = 0;
		int[] coordinates = new int[] { 0, 0 };
		for(int i = 0; i < gridSize; i++) {
			for(int j = 0; j < gridSize; j++) {
				if(matrix[i][j] > progress_count) {
					progress_count = matrix[i][j];
					coordinates = new int[] { i, j };
				}
			}
		}
		return coordinates;
	}

	private static int[][] findLegalMoves(int[][] matrix, int[] last_coordinates) {
		return findLegalMoves(matrix, last_coordinates, true);
	}

	private static int[][] findLegalMoves(int[][] matrix, int[] last_coordinates, Boolean peek_ahead) {
		ArrayList<int[]> available_moves = new ArrayList<int[]>();
		for (DirectionDeltas delta : DirectionDeltas.values()) {
			int[] offset = delta.getDeltas();
			int new_row = offset[0] + last_coordinates[0];
			int new_col = offset[1] + last_coordinates[1];
			if(new_row >= gridSize || new_col >= gridSize || new_row < 0 || new_col < 0)
				continue;

			// if(0 != matrix[new_row][new_col])
			// 	continue;

		    int[] new_coordinates = new int[] { new_row, new_col };
		    available_moves.add(new_coordinates);
		}
		int results_length = (peek_ahead) ? 3 : 2;
		int[][] results = new int[available_moves.size()][results_length];
		for(int i = 0; i < available_moves.size(); i++) {
			int[] move_coordinates = available_moves.get(i);
			results[i][0] = move_coordinates[0];
			results[i][1] = move_coordinates[1];
			// third index reserved for sorting by possible moves available
			if(peek_ahead) {
				int[][] peeked_moves = findLegalMoves(matrix, new int[] { results[i][0], results[i][1] }, false);
				results[i][2] = peeked_moves.length;
			}
		}
		// @todo: implement Warnsdorff heuristic to sort available options
		if(peek_ahead) {
			// for(int i = 0; i < results.length; i++) {
			// 	int[][] peeked_moves = findLegalMoves(matrix, new int[] { results[i][0], results[i][1] }, false);
			// 	results[i][2] = peeked_moves.length;
			// }
			Arrays.sort(results, new Comparator<int[]>() {
				@Override
				public int compare(final int[] entry1, final int[] entry2) {
					final int field1 = entry1[2];
					final int field2 = entry2[2];
					return Integer.compare(field1, field2);
				}
			});
			int[][] new_results = new int[results.length][2];
			for(int i = 0; i < results.length; i++) {
				new_results[i][0] = results[i][0];
				new_results[i][1] = results[i][1];
			}
			results = new_results;
		}

		return results;
	}

	private static Boolean isComplete(int[][] tour_progress, int[] last_coordinates) {
		for(int i = 0; i < gridSize; i++) {
			for(int j = 0; j < gridSize; j++) {
				if(0 == tour_progress[i][j])
					return false;
			}
		}
		if(!is_closed_tour)
			return true;
		for (DirectionDeltas delta : DirectionDeltas.values()) {
			int[] offset = delta.getDeltas();
			int new_row = offset[0] + last_coordinates[0];
			int new_col = offset[1] + last_coordinates[1];
		    int[] new_coordinates = new int[] { new_row, new_col };
			if(Arrays.equals(new_coordinates, starting_coordinates))
				return true;
		}
		return false;
	}

	private static int[][] move(int[][] tour_progress, int[] last_coordinates, int move_count) {
		attempt_count += 1;
		int[][] available_moves = findLegalMoves(tour_progress, last_coordinates);
		for(int i = 0; i < available_moves.length; i++) {
			int[][] progress_copy = new int[gridSize][gridSize];
			for (int j = 0; j < gridSize; j++)
			    progress_copy[j] = Arrays.copyOf(tour_progress[j], tour_progress[j].length);

			int new_row = available_moves[i][0];
			int new_col = available_moves[i][1];
			if(0 != progress_copy[new_row][new_col])
				continue;
			progress_copy[new_row][new_col] = move_count + 1;

			if(isComplete(progress_copy, available_moves[i]))
				return progress_copy;

			int[][] attempt = move(progress_copy, available_moves[i], move_count + 1);
			int[] latest_coordinates = findLastMove(attempt);
			if(isComplete(attempt, latest_coordinates))
				return attempt;
		}

		return new int[gridSize][gridSize];
	}

	private static void printGrid(int[][] matrix) {
		for(int i = 0; i < gridSize; i++) {
			if(i == 0)
				System.out.print("  ");
			System.out.print("  " + (i+1) + " ");
		}
		System.out.print("\n");

		for(int i = 0; i < gridSize; i++) {
			System.out.print((i+1) + " ");
			for(int j = 0; j < gridSize; j++) {
				String mark = (matrix[i][j] != 0) ? String.valueOf(matrix[i][j]) : " ";
				if(matrix[i][j] < 10)
					mark = " " + mark;
				System.out.print("[" + mark + "]");
			}
			System.out.println("");
		}
	}

	private static int promptPlacement(Scanner sc, String type) {
		return promptPlacement(sc, type, false);
	}

	private static int promptPlacement(Scanner sc, String type, Boolean try_again) {
		if(try_again)
			System.out.println("Sorry, that is not a valid entry! Please try again!");

		System.out.print("\nPlease enter which " + type + " to place the knight: ");
		int index = sc.nextInt();

		return (index <= 0 || index > gridSize) ? promptPlacement(sc, type, true) : index;
	}

	public static void main(String []args) {
		int[][] matrix = new int[gridSize][gridSize];
		printGrid(matrix);

		Scanner sc = new Scanner(System.in);
		int user_row = promptPlacement(sc, "row") - 1;
		int user_col = promptPlacement(sc, "column") - 1;
		starting_coordinates = new int[] { user_row, user_col };
		matrix[user_row][user_col] = 1;

		int[][] complete_matrix = move(matrix, starting_coordinates, 1);
		printGrid(complete_matrix);
		System.out.println("attempted " + attempt_count + " moves.");
	}
}