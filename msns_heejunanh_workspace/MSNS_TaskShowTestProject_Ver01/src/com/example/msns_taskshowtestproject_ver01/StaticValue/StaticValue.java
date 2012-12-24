package com.example.msns_taskshowtestproject_ver01.StaticValue;

public class StaticValue {
	/**몇초마다 저장하는지 표시하는 값
	 * 부팅시 초기화 해야함. */
	public static long NUMBEROFALARMTERM = 3000;
	
	/**사용/중지 flasg를 주어 표시
	 * 사용처
	 * 1. 부팅이 완료되면 SP를 확인하여 값을 세팅한다.
	 * 2. 부팅이 완료
	 * 	2-1. 값이 true이면 저장 알고리즘을 시작한다.
	 * 	2-2. 값이 false
	 * 		정해야함.
	 * 3. 사용자가 저장 알고리즘을 시작하겠다고 하면
	 * true로 변경
	 * 4. 사용자가 저장 알고리즘을 중지 하겠다고 하면
	 * flase로 변경.
	 * 
	 * */
	public static boolean RUNNING_FLAG;
	
}
