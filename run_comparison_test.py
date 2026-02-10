#!/usr/bin/env python3
"""
Redis seqId vs No Redis 성능 비교 테스트
"""

import subprocess
import json
import time
from datetime import datetime

def run_test(test_name, config):
    """단일 테스트 실행"""
    print(f"\n{'='*60}")
    print(f"🧪 테스트 시작: {test_name}")
    print(f"{'='*60}")
    
    # performance_test.py 실행
    # 실제로는 config에 따라 다른 엔드포인트를 사용하도록 수정 필요
    result = subprocess.run(
        ['python3', 'performance_test.py'],
        capture_output=True,
        text=True
    )
    
    print(result.stdout)
    
    # 결과 파일 찾기 (가장 최근 파일)
    import glob
    result_files = glob.glob('performance_result_*.json')
    if result_files:
        latest_file = max(result_files, key=lambda x: x.split('_')[-1])
        with open(latest_file, 'r') as f:
            return json.load(f)
    return None

def compare_results(redis_result, no_redis_result):
    """결과 비교 및 출력"""
    print("\n" + "="*60)
    print("📊 성능 비교 결과")
    print("="*60)
    
    if not redis_result or not no_redis_result:
        print("⚠️  결과 파일을 찾을 수 없습니다.")
        return
    
    redis_res = redis_result['results']
    no_redis_res = no_redis_result['results']
    
    # 처리량 비교
    throughput_improvement = (
        (redis_res['throughput'] - no_redis_res['throughput']) 
        / no_redis_res['throughput'] * 100
    )
    
    # 지연시간 비교
    latency_improvement = (
        (no_redis_res['avg_latency_ms'] - redis_res['avg_latency_ms'])
        / no_redis_res['avg_latency_ms'] * 100
    )
    
    print("\n1️⃣  처리량 (Throughput)")
    print(f"   Redis:    {redis_res['throughput']:.2f} msg/sec")
    print(f"   No Redis: {no_redis_res['throughput']:.2f} msg/sec")
    print(f"   개선율:   {throughput_improvement:+.2f}% {'✅' if throughput_improvement > 0 else '❌'}")
    
    print("\n2️⃣  평균 지연시간 (Avg Latency)")
    print(f"   Redis:    {redis_res['avg_latency_ms']:.2f} ms")
    print(f"   No Redis: {no_redis_res['avg_latency_ms']:.2f} ms")
    print(f"   개선율:   {latency_improvement:+.2f}% {'✅' if latency_improvement > 0 else '❌'}")
    
    print("\n3️⃣  순서 정확도 (Order Accuracy)")
    print(f"   Redis:    {redis_res['order_accuracy']:.2f}%")
    print(f"   No Redis: {no_redis_res['order_accuracy']:.2f}%")
    print(f"   순서 오류:")
    print(f"     Redis:    {redis_res['order_errors']} 건")
    print(f"     No Redis: {no_redis_res['order_errors']} 건 {'⚠️' if no_redis_res['order_errors'] > 0 else '✅'}")
    
    # 종합 평가
    print("\n" + "="*60)
    print("🎯 종합 평가")
    print("="*60)
    
    score = 0
    if throughput_improvement > 20:
        print("✅ 처리량: Redis 방식이 20% 이상 우수")
        score += 1
    else:
        print("⚠️  처리량: 개선 효과 미미")
    
    if latency_improvement > 30:
        print("✅ 지연시간: Redis 방식이 30% 이상 우수")
        score += 1
    else:
        print("⚠️  지연시간: 개선 효과 미미")
    
    if redis_res['order_accuracy'] == 100 and no_redis_res['order_accuracy'] < 95:
        print("✅ 순서 정확도: Redis 방식이 완벽한 순서 보장")
        score += 1
    else:
        print("⚠️  순서 정확도: 차이 미미")
    
    print(f"\n📊 최종 점수: {score}/3")
    
    if score >= 2:
        print("🎉 Redis seqId 방식의 우수성이 증명되었습니다!")
    else:
        print("🤔 추가 테스트 또는 환경 조정이 필요합니다.")
    
    # 결과 저장
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    comparison_file = f"comparison_result_{timestamp}.json"
    
    with open(comparison_file, 'w') as f:
        json.dump({
            'timestamp': timestamp,
            'redis_result': redis_res,
            'no_redis_result': no_redis_res,
            'improvements': {
                'throughput_improvement_percent': throughput_improvement,
                'latency_improvement_percent': latency_improvement,
            },
            'score': score
        }, f, indent=2)
    
    print(f"\n💾 비교 결과 저장: {comparison_file}")

def main():
    print("🚀 채팅 시스템 성능 비교 테스트")
    print("Redis seqId vs DB AUTO_INCREMENT")
    
    # 테스트 1: Redis seqId 방식
    print("\n" + "="*60)
    print("1️⃣  Redis seqId 방식 테스트 준비")
    print("="*60)
    print("⚠️  채팅 서버가 실행 중인지 확인하세요!")
    print("⚠️  Redis가 실행 중인지 확인하세요!")
    input("준비되면 Enter를 누르세요...")
    
    redis_result = run_test("Redis seqId", {})
    
    # 잠시 대기 (서버 안정화)
    print("\n⏳ 서버 안정화 대기 (10초)...")
    time.sleep(10)
    
    # 테스트 2: No Redis 방식
    print("\n" + "="*60)
    print("2️⃣  No Redis 방식 테스트 준비")
    print("="*60)
    print("⚠️  performance_test.py에서 엔드포인트를 /pub/chat/message-no-redis로 변경하세요!")
    print("   또는 ChatController에서 기본 엔드포인트를 임시로 변경하세요.")
    input("준비되면 Enter를 누르세요...")
    
    no_redis_result = run_test("No Redis", {})
    
    # 결과 비교
    if redis_result and no_redis_result:
        compare_results(redis_result, no_redis_result)
    else:
        print("\n⚠️  테스트 결과를 불러올 수 없습니다.")

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n⚠️  테스트가 중단되었습니다.")
