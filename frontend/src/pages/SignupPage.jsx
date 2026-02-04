import { Link } from 'react-router-dom';
import { useSignupForm } from '../hooks/auth/useSignupForm';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';

export default function SignupPage() {
  const { formMethods, serverError, onSubmit, isSubmitting } = useSignupForm();
  const { register, formState: { errors } } = formMethods;

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-zinc-900 text-white">
      <div className="w-full max-w-md p-8 bg-zinc-800 rounded-lg shadow-lg">
        <h2 className="text-2xl font-bold text-center mb-6">계정 만들기</h2>
        
        <form onSubmit={onSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-zinc-400 mb-1 uppercase">이메일</label>
            <Input {...register('email')} type="email" />
            {errors.email && <p className="text-red-400 text-sm mt-1">{errors.email.message}</p>}
          </div>

          <div>
            <label className="block text-xs font-bold text-zinc-400 mb-1 uppercase">사용자명 (별명)</label>
            <Input {...register('username')} />
            {errors.username && <p className="text-red-400 text-sm mt-1">{errors.username.message}</p>}
          </div>

          <div>
            <label className="block text-xs font-bold text-zinc-400 mb-1 uppercase">비밀번호</label>
            <Input {...register('password')} type="password" />
            {errors.password && <p className="text-red-400 text-sm mt-1">{errors.password.message}</p>}
          </div>

          {serverError && <p className="text-red-400 text-sm">{serverError}</p>}

          <Button type="submit" disabled={isSubmitting} className="w-full">
            계속하기
          </Button>
        </form>

        <div className="mt-4 text-sm text-zinc-400">
          이미 계정이 있으신가요? <Link to="/login" className="text-blue-400 hover:underline">로그인하기</Link>
        </div>
      </div>
    </div>
  );
}
