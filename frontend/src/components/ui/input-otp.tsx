// @ts-nocheck
"use client";
import { InputOTP, InputOTPGroup, InputOTPSlot, InputOTPSeparator } from "@/components/ui/input-otp";

export function OTPExample() {
  const [value, setValue] = React.useState("");

  return (
    <div className="flex flex-col items-center space-y-4">
      <InputOTP maxLength={6} value={value} onChange={(val) => setValue(val)}>
        <InputOTPGroup>
          <InputOTPSlot index={0} />
          <InputOTPSlot index={1} />
          <InputOTPSlot index={2} />
        </InputOTPGroup>

        <InputOTPSeparator />

        <InputOTPGroup>
          <InputOTPSlot index={3} />
          <InputOTPSlot index={4} />
          <InputOTPSlot index={5} />
        </InputOTPGroup>
      </InputOTP>

      <p className="text-sm text-muted-foreground">Entered: {value}</p>
    </div>
  );
}
