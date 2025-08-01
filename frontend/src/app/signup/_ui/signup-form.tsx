'use client';

import { useActionState } from "react";
import { signupAction } from "../../_libs/actions/signup-action";

export default function SignupForm() {
  const [state, action, pending] = useActionState(signupAction, undefined);  
 
  return (
    <form action={action}>
      <div>
        <label htmlFor="username">Username: </label>
        <input id="username" name="username" placeholder="Username" />
      </div>
      {state?.errors?.username && <p>{state.errors.username}</p>}
 
      <div>
        <label htmlFor="email">Email: </label>
        <input id="email" name="email" placeholder="Email" />
      </div>
      {state?.errors?.email && <p>{state.errors.email}</p>}
 
      <div>
        <label htmlFor="password">Password: </label>
        <input id="password" name="password" type="password" />
      </div>
      {state?.errors?.password && (
        <div>
          <p>Password must:</p>
          <ul>
            {state.errors.password.map((error: string) => (
              <li key={error}>- {error}</li>
            ))}
          </ul>
        </div>
      )}
      <button disabled={pending} type="submit">
        회원가입
      </button>
      {state?.message && (
        <p className="text-red-500 mt-2">{state.message}</p>
      )}
    </form>
  );
}